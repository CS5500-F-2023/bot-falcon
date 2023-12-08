package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.BattleController;
import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.NPCBattle;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class BattleCommand implements SlashCommandHandler, StringSelectHandler {

    static final String NAME = "battle";

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject BattleController battleController;

    @Inject
    public BattleCommand() {
        // Defined public and empty for Dagger injection
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {
        return Commands.slash(
                getName(), "Start a battle with a random Pokémon, with a cost of only 5 coins");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        log.info("event: /battle");

        String trainerDiscordId = event.getMember().getId();
        List<Pokemon> pokemonInventory =
                trainerController.getTrainerPokemonInventory(trainerDiscordId);

        // Check if the user have any pokemon to start the batthe
        if (pokemonInventory.isEmpty()) {
            event.reply(
                            String.format(
                                    "Oops.... <@%s>, you don't have Pokémon to start the battle!",
                                    trainerDiscordId))
                    .queue();
            return;
        }

        // Check if the user have enough coins to start the battle
        try {
            trainerController.decreaseTrainerBalance(trainerDiscordId, 5);
        } catch (InsufficientBalanceException e) {
            event.reply(
                            String.format(
                                    "Oops.... <@%s>, you don't have enough coins to start the battle!",
                                    trainerDiscordId))
                    .queue();
            return;
        }

        // Build embedded message with instructions
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Select a Pokémon to use in the battle");
        embedBuilder.setDescription(
                "Hint: Select \"Maybe next time\" from the list if you change your mind");

        // Build the dropdown menu
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(NAME);
        menuBuilder.setPlaceholder("Choose your Pokémon");
        menuBuilder.addOption("Maybe next time", "maybe-next-time" + ":" + trainerDiscordId);
        for (Pokemon pokemon : pokemonInventory) {
            PokemonSpecies species =
                    pokedexController.getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());
            menuBuilder.addOption(
                    species.getName(), pokemon.getId().toString() + ":" + trainerDiscordId);
        }

        // Send the embedded message with menu list
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(menuBuilder.build());
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        // Process the response
        final String response = event.getInteraction().getValues().get(0);
        String[] fields = response.split(":");
        String trPokemonID = fields[0];
        String initiatorDiscordId = fields[1];
        String trDiscordId = event.getMember().getId();

        // If user who clicks list is Not the same as who initiated the list
        if (!trDiscordId.equals(initiatorDiscordId)) {
            event.reply(String.format("Sorry <@%s>, access denied.", trDiscordId)).queue();
            return;
        }

        // If user chooses not to battle for now
        if (trPokemonID.equals("maybe-next-time")) {
            trainerController.increaseTrainerBalance(trDiscordId, 5);
            event.reply(String.format("<@%s>, you decide not to battle.", trDiscordId)).queue();
            return;
        }

        MessageEmbed trPokeProfile = buildPokemonProfile(trPokemonID, "You have chosen");
        MessageEmbed trPokeStat = buildPokemonStat(trPokemonID);

        NPCBattle battle = battleController.setUpNewBattle(trDiscordId, trPokemonID);

        Pokemon npcPokemon = battle.getNpcPokemon();
        String npcPokemonID = npcPokemon.getId().toString();
        MessageEmbed npcPokeProfile = buildPokemonProfile(npcPokemonID, "Your opponent Pokemon is");
        MessageEmbed npcPokeStat = buildPokemonStat(npcPokemonID);

        // Start battle and get the battle record
        battleController.runBattle(battle);

        // Build up and send the battle rounds and result messages
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addEmbeds(trPokeProfile, trPokeStat, npcPokeProfile, npcPokeStat);
        event.reply(messageCreateBuilder.build())
                .queue(
                        interactionHook -> {
                            scheduler.schedule(
                                    () ->
                                            interactionHook
                                                    .sendMessage(battle.getStartMessage())
                                                    .queue(),
                                    3,
                                    TimeUnit.SECONDS);

                            // Send round info
                            for (String roundMsg : battle.getRoundMessages()) {
                                scheduler.schedule(
                                        () -> interactionHook.sendMessage(roundMsg).queue(),
                                        5,
                                        TimeUnit.SECONDS);
                            }

                            // Send result info
                            scheduler.schedule(
                                    () ->
                                            interactionHook
                                                    .sendMessage(battle.getResultMessage())
                                                    .queue(),
                                    5,
                                    TimeUnit.SECONDS);
                        });
    }

    private MessageEmbed buildPokemonProfile(String pokemonIdStr, String msg) {
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);
        Integer pokedex = pokemon.getPokedexNumber();
        PokemonSpecies species = pokedexController.getPokemonSpeciesByPokedex(pokedex);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(String.format("%s %s!", msg, species.getName()));
        embedBuilder.setImage(species.getImageUrl());
        return embedBuilder.build();
    }

    private MessageEmbed buildPokemonStat(String pokemonIdStr) {
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);
        Integer pokedex = pokemon.getPokedexNumber();
        String speciesInfoStr = pokedexController.buildSpeciesDetails(pokedex);
        String pokeInfoStr = pokemonController.buildPokemonStats(pokemonIdStr);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(String.format("%s%n%s", speciesInfoStr, pokeInfoStr));
        return embedBuilder.build();
    }
}

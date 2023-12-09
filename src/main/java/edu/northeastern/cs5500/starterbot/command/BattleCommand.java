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
                    pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
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

        // The user chooses the his or her Pokémon, being the Trainer Pokémon
        Pokemon trPokemon = pokemonController.getPokemonById(trPokemonID);
        trPokemon.setCurrentHp(trPokemon.getHp());
        Integer trPokedex = trPokemon.getPokedexNumber();
        PokemonSpecies trPokeSpecies = pokedexController.getPokemonSpeciesByREALPokedex(trPokedex);
        String trPokeSpeciesInfoStr = pokedexController.buildSpeciesDetails(trPokedex);
        String trPokeInfoStr = pokemonController.buildPokemonStats(trPokemonID);
        String trPokeName = trPokeSpecies.getName();

        // Set up the Embedded Message for Trainer Pokémon
        EmbedBuilder embedBuilder1 = new EmbedBuilder();
        embedBuilder1.setTitle(String.format("You have chosen %s!", trPokeName));
        embedBuilder1.addField(
                "----\n🔎 Details of your chosen Pokémon!\n----",
                String.format("```%s%n%s%n```", trPokeSpeciesInfoStr, trPokeInfoStr),
                false);
        embedBuilder1.setThumbnail(trPokeSpecies.getImageUrl());

        // Set up the battle
        NPCBattle battle = battleController.setUpNewBattle(trDiscordId, trPokemonID);

        // The NPC Pokémon is accessible by calling battle.getNpcPokemon()
        Pokemon npcPokemon = battle.getNpcPokemon();
        Integer npcPokedex = npcPokemon.getPokedexNumber();
        PokemonSpecies npcPokeSpecies =
                pokedexController.getPokemonSpeciesByREALPokedex(npcPokedex);
        String npcPokeSpeciesInfoStr = pokedexController.buildSpeciesDetails(npcPokedex);
        String npcPokeInfoStr = pokemonController.buildPokemonStats(npcPokemon.getId().toString());
        String npcPokeName = npcPokeSpecies.getName();

        // Set up the Embedded Message for NPC Pokémon
        EmbedBuilder embedBuilder2 = new EmbedBuilder();
        embedBuilder2.setTitle(String.format("Your opponent's Pokémon is %s!", npcPokeName));
        embedBuilder2.addField(
                "----\n🔎 Details of your opponent's Pokémon!\n----",
                String.format("```%s%n%s%n```", npcPokeSpeciesInfoStr, npcPokeInfoStr),
                false);
        embedBuilder2.setThumbnail(npcPokeSpecies.getImageUrl());

        // Run the battle
        battleController.runBattle(battle);

        // Build up and send the battle messages
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addEmbeds(embedBuilder1.build(), embedBuilder2.build());
        event.reply(messageCreateBuilder.build())
                .queue(
                        interactionHook -> {
                            for (String msg : battle.getMessages()) {
                                scheduler.schedule(
                                        () -> interactionHook.sendMessage(msg).queue(),
                                        3,
                                        TimeUnit.SECONDS);
                            }
                        });
    }
}

package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import java.util.List;
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
                getName(),
                "Start a battle with a random Pokémon. \nIt costs 5 coins to start a battle");
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

        // Build dropdown Menu
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(NAME);
        menuBuilder.setPlaceholder("Choose your Pokémon");
        menuBuilder.addOption("Maybe next time", "maybe-next-time" + ":" + trainerDiscordId);

        // Adding Pokemon options to the menu
        for (Pokemon pokemon : pokemonInventory) {
            PokemonSpecies species =
                    pokedexController.getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());
            menuBuilder.addOption(
                    species.getName(), pokemon.getId().toString() + ":" + trainerDiscordId);
        }

        // Reply with both the embedded message and menu list
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
        log.error("!!! onStringSelectInteraction: " + "trPokemonID: " + trPokemonID);
        String initiatorDiscordId = fields[1];
        String trDiscordId = event.getMember().getId();
        log.error("!!! onStringSelectInteraction: " + "trDiscordId: " + initiatorDiscordId);

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
        PokemonSpecies trPokeSpecies = pokedexController.getPokemonSpeciesByPokedex(trPokedex);
        String trPokeSpeciesInfoStr = pokedexController.buildSpeciesDetails(trPokedex);
        log.error("!!! trPokedex.toString(): " + trPokedex.toString());
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

        // The bot will pick a random pokemon, being the NPC Pokémon
        Pokemon npcPokemon = pokemonController.spawnRandonPokemon();
        Integer npcPokedex = npcPokemon.getPokedexNumber();
        PokemonSpecies npcPokeSpecies = pokedexController.getPokemonSpeciesByPokedex(npcPokedex);
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

        // Send the battle set up message (which Pokémon battles with which Pokémon)
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addEmbeds(embedBuilder1.build(), embedBuilder2.build());
        // event.reply(messageCreateBuilder.build()).queue();

        // Send subsequent message (round update and battle results)
        event.reply(messageCreateBuilder.build())
                .queue(
                        interactionHook -> {
                            interactionHook.sendMessage("Round message 1").queue();
                            interactionHook.sendMessage("Round message 2").queue();
                            interactionHook.sendMessage("Round message 3").queue();
                            interactionHook.sendMessage("Battle result message").queue();
                        });
    }
}

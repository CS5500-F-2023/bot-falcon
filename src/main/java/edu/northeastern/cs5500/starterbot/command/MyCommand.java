package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class MyCommand implements SlashCommandHandler {

    static final String NAME = "my";

    @Inject TrainerController trainerController;
    @Inject PokemonController pokemonController;
    @Inject PokedexController pokedexController;

    @Inject
    public MyCommand() {
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
        return Commands.slash(getName(), "Get your pokemon's stats by typing the number!")
                .addOption(
                        OptionType.INTEGER,
                        "pokemon",
                        "The bot will reply to your command with the pokemon stats",
                        true);
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /my");
        String trainerDiscordId = event.getUser().getId();
        try {
            Integer pokemonInventoryIndex = event.getOption("pokemon").getAsInt() - 1;

            Pokemon pokemon =
                    trainerController.getPokemonFromInventory(
                            trainerDiscordId, pokemonInventoryIndex);
            String pokemonIdString = pokemon.getId().toString();
            String pokemonDetails = pokemonController.buildPokemonStats(pokemonIdString);
            String speciesDetails =
                    pokedexController.buildSpeciesDetails(pokemon.getPokedexNumber());

            StringBuilder pokeStatsBuilder = new StringBuilder();
            pokeStatsBuilder.append("```");
            pokeStatsBuilder.append(speciesDetails);
            pokeStatsBuilder.append(pokemonDetails).append("```");
            event.reply(pokeStatsBuilder.toString()).queue();
        } catch (InvalidInventoryIndexException e) {
            event.reply("Please enter a valid number.").queue();
        }
    }
}
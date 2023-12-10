package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
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
        return Commands.slash(getName(), "Get your pokemon's stats by typing its number!")
                .addOption(
                        OptionType.INTEGER,
                        "pokemon",
                        "The bot will reply with the pokemon stats",
                        true);
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /my");
        try {
            String trainerDiscordId = event.getUser().getId();
            Integer pokemonInventoryIndex = event.getOption("pokemon").getAsInt() - 1;
            Pokemon pokemon =
                    trainerController.getPokemonFromInventory(
                            trainerDiscordId, pokemonInventoryIndex);
            PokemonSpecies species =
                    pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());

            String pokeProfile =
                    buildPokemonProfile(species, pokemon, pokemonInventoryIndex);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setThumbnail(species.getImageUrl());
            embedBuilder.addField("Your Pokemon Detail\n", pokeProfile, false);
            event.replyEmbeds(embedBuilder.build()).queue();

        } catch (InvalidInventoryIndexException e) {
            event.reply("Oops...the pokemon does not exist, try again").queue();
        }
    }

    /**
     * Build the pokemon profile with the pokemon id
     *
     * @param trainerDiscordId the trainer's discord id
     * @param pokemon the pokemon
     * @return the pokemon profile
     */
    private String buildPokemonProfile(PokemonSpecies species, Pokemon pokemon, Integer inventoryIndex) {
        String pokemonDetails = pokemon.buildPokemonStats(pokemon);
        String speciesDetails = species.buildSpeciesDetails(species);
        String boardLine = "\n----------------------------\n";

        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder
                .append(speciesDetails)
                .append("\n")
                .append("🌠 Pokemon Stats 🌠")
                .append(boardLine)
                .append(String.format("PokeID. : 🔢 %d\n", inventoryIndex))
                .append(pokemonDetails)
                .append("\n")
                .append("📈 Pokemon XP Progress 📈")
                .append(boardLine)
                .append("XP      : ")
                .append(pokemon.generateXpProgressBar())
                .append(String.format(" %d/%d", pokemon.getExPoints(), pokemon.LEVEL_UP_THRESHOLD))
                .append("\n\n🥣 Boost your pokemon using /feed with PokeID!");

        return "```" + profileBuilder.toString() + "```";
    }
}

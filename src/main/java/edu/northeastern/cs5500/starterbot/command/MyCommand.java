package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InvalidPokemonException;
import edu.northeastern.cs5500.starterbot.model.BotConstants;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
            Trainer trainer = trainerController.getTrainerForMemberId(trainerDiscordId);
            String pokemonIdStr = trainer.getTrainerPokemonIdByIndex(pokemonInventoryIndex);

            MessageEmbed pokemonProfileEmbed = buildPokemonProfile(pokemonIdStr);
            MessageEmbed pokemonStatEmbed = buildPokemonStats(pokemonIdStr, pokemonInventoryIndex);
            event.getChannel().sendMessageEmbeds(pokemonProfileEmbed, pokemonStatEmbed).queue();

        } catch (InvalidPokemonException e) {
            event.reply("Oops...the pokemon does not exist, try again").queue();
        }
    }

    /**
     * Builds a MessageEmbed object for displaying a Pokemon profile.
     *
     * @param pokemonIdStr the ID of the Pokemon
     * @return the MessageEmbed object representing the Pokemon profile
     */
    private MessageEmbed buildPokemonProfile(String pokemonIdStr) {
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);
        Integer pokedex = pokemon.getPokedexNumber();
        PokemonSpecies species = pokedexController.getPokemonSpeciesByREALPokedex(pokedex);

        StringBuilder sb = new StringBuilder();
        sb.append("```");
        sb.append(String.format("🔍 Learn how to grow your %s!```", species.getName()));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Your Pokemon Handbook");
        embedBuilder.setDescription(sb.toString());
        embedBuilder.setImage(species.getImageUrl());
        embedBuilder.setColor(species.getSpeciesColor());
        return embedBuilder.build();
    }

    /**
     * Builds a MessageEmbed object containing the details and stats of a Pokemon.
     *
     * @param pokemonIdStr the ID of the Pokemon
     * @return a MessageEmbed object representing the Pokemon's details and stats
     */
    private MessageEmbed buildPokemonStats(String pokemonIdStr, Integer inventoryIndex) {
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);
        Integer pokedex = pokemon.getPokedexNumber();
        PokemonSpecies species = pokedexController.getPokemonSpeciesByREALPokedex(pokedex);

        String speciesDetail = species.buildSpeciesDetails();
        String pokemonDetail = buildPokemonStatsString(pokemonIdStr, inventoryIndex);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(String.format("```%s\n%s```", speciesDetail, pokemonDetail));
        embedBuilder.setColor(species.getSpeciesColor());
        return embedBuilder.build();
    }

    /**
     * Build the pokemon profile with the pokemon id
     *
     * @param trainerDiscordId the trainer's discord id
     * @param pokemon the pokemon
     * @return the pokemon profile
     */
    private String buildPokemonStatsString(String pokemonIdStr, Integer inventoryIndex) {
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);

        String pokemonDetails = pokemon.buildPokemonStats();

        String boardLine = "\n----------------------------\n";
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder
                .append("🌠 Pokemon Stats 🌠")
                .append(boardLine)
                .append(String.format("PokeID. : 🔢 %d\n", inventoryIndex + 1))
                .append(pokemonDetails)
                .append("\n")
                .append("📈 Pokemon XP Progress 📈")
                .append(boardLine)
                .append("XP      : ")
                .append(pokemon.generateXpProgressBar())
                .append(
                        String.format(
                                " %d/%d",
                                pokemon.getExPoints(), BotConstants.POKE_LEVEL_UP_THRESHOLD))
                .append("\n\n🥣 Boost your pokemon using /feed with PokeID!");

        return profileBuilder.toString();
    }
}

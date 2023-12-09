package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class SpawnCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "spawn";

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    public SpawnCommand() {
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
        return Commands.slash(getName(), "Spawn a random Pokemon for the user to try to catch");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /spawn");
        Pokemon pokemon = pokemonController.spawnRandonPokemon();
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());

        String pokemonDetails = pokemonController.buildPokemonStats(pokemon.getId().toString());
        String pokemonSpeciesDetail =
                pokedexController.buildSpeciesDetails(pokemon.getPokedexNumber());

        String trainerDiscordId = event.getMember().getId();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(String.format("A wild %s appear!", species.getName()));
        embedBuilder.setDescription(
                String.format(
                        "It costs %d coins to catch %s. What will you do?",
                        pokemon.getCatchCosts(), species.getName()));
        embedBuilder.addField(
                String.format("----\n🔎 Learn more about %s!\n----", species.getName()),
                String.format("```%s%s```", pokemonSpeciesDetail, pokemonDetails),
                false);
        embedBuilder.setThumbnail(species.getImageUrl());

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder =
                messageCreateBuilder.addActionRow(
                        Button.primary(
                                getName()
                                        + ":catch:"
                                        + pokemon.getId().toString()
                                        + ":"
                                        + trainerDiscordId,
                                "Catch"),
                        Button.secondary(
                                getName()
                                        + ":letgo:"
                                        + pokemon.getId().toString()
                                        + ":"
                                        + trainerDiscordId,
                                "Let Go"));
        messageCreateBuilder = messageCreateBuilder.addEmbeds(embedBuilder.build());
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String[] buttonIdParts = event.getButton().getId().split(":");
        String action = buttonIdParts[1]; // "catch" or "letgo"
        String pokemonID = buttonIdParts[2];
        String initiateTrainerDiscordId = buttonIdParts[3];
        String trainerDiscordId = event.getMember().getId();
        Pokemon pokemon = pokemonController.getPokemonById(pokemonID);
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());

        // Handle the button interaction
        if (action.equals("catch") && trainerDiscordId.equals(initiateTrainerDiscordId)) {
            // Handle the 'Catch' action
            int costs = pokemon.getCatchCosts();
            try {
                trainerController.decreaseTrainerBalance(trainerDiscordId, costs);
                trainerController.addPokemonToTrainer(trainerDiscordId, pokemonID);
                event.reply(
                                String.format(
                                        "<@%s>, you caught a %s !",
                                        trainerDiscordId, species.getName()))
                        .queue();
                event.getMessage()
                        .editMessageEmbeds(messageEmbed)
                        .setComponents()
                        .queue(); // disable button
            } catch (InsufficientBalanceException e) {
                event.reply(
                                String.format(
                                        "<@%s>, you don't have enough coins to catch the %s!",
                                        trainerDiscordId, species.getName()))
                        .queue();
                event.getMessage().editMessageEmbeds(messageEmbed).setComponents().queue();
            }
        } else if (action.equals("letgo") && trainerDiscordId.equals(initiateTrainerDiscordId)) {
            // Handle the 'Let Go' action
            event.reply(
                            String.format(
                                    "<@%s>, you decide to let %s go!",
                                    trainerDiscordId, species.getName()))
                    .queue();
            event.getMessage().editMessageEmbeds(messageEmbed).setComponents().queue();
        } else {
            event.reply(
                            String.format(
                                    "Sorry <@%s>, you are not authorized to perform this action.",
                                    trainerDiscordId))
                    .queue();
        }
    }
}

package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
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
                pokedexController.getePokemonSpeciesByNumber(pokemon.getPokedexNumber());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(String.format("A wild %s appear!", species.getName()));
        embedBuilder.addField("Level", Integer.toString(pokemon.getLevel()), false);
        embedBuilder.setThumbnail(species.getImageUrl());

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder =
                messageCreateBuilder.addActionRow(
                        Button.primary(
                                getName() + ":catch:" + pokemon.getId().toString(), "Catch"));
        messageCreateBuilder = messageCreateBuilder.addEmbeds(embedBuilder.build());
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        // Must mean the user clicked Catch
        String trainerDiscordId = event.getMember().getId();
        String pokemonID = event.getButton().getId().split(":")[2];
        trainerController.addPokemonToTrainer(trainerDiscordId, pokemonID);
        event.reply(
                        String.format(
                                "Player %s caught Pokemon %s",
                                trainerDiscordId, event.getButton().getId()))
                .queue();
    }
}

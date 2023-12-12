package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.model.BotConstants;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class PokemonCommand implements SlashCommandHandler {

    static final String NAME = "pokemon";

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    public PokemonCommand() {
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
        return Commands.slash(getName(), "View your pokemon status");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /pokemon");

        String trainerDiscordId = event.getMember().getId();

        List<Pokemon> pokemonInventory =
                trainerController.getTrainerPokemonInventory(trainerDiscordId);

        String pokemonInventoryDetail =
                trainerController.buildPokemonInventoryDetail(pokemonInventory);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(pokemonInventoryDetail);
        embedBuilder.setColor(BotConstants.COLOR_TRAINER);
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}

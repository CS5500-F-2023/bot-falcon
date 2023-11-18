package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class StatusCommand implements SlashCommandHandler {

    static final String NAME = "status";

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    public StatusCommand() {
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
        return Commands.slash(getName(), "View your current status");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /status");

        String trainerDiscordId = event.getMember().getId();

        Map<String, String> trainerStats = trainerController.getTrainerStats(trainerDiscordId);
        String trainerBalance = trainerStats.get("Balance");
        String trainerPokemonNumbers = trainerStats.get("PokemonNumbers");
        String trainerPokemonInventory = trainerStats.get("PokemonInventory");

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Your Stats");

        embedBuilder.addField("Balance", trainerBalance, true);
        embedBuilder.addField("Total Pokemon", trainerPokemonNumbers, true);
        if (!trainerPokemonNumbers.equals("0")) {
            embedBuilder.addField("Inventory Members", trainerPokemonInventory, false);
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addEmbeds(embedBuilder.build());
        event.reply(messageCreateBuilder.build()).queue();
    }
}

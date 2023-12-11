package edu.northeastern.cs5500.starterbot.command;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class HelpCommand implements SlashCommandHandler {

    static final String NAME = "help";

    @Inject
    public HelpCommand() {
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
        return Commands.slash(getName(), "Display all of the available commands in Pokebot!");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /help");

        event.reply(
                        String.format(
                                "```🌟🌟🌟Available Pokebot Commands 🌟🌟🌟\n\n"
                                        + "🌈👾 Pokemon 👾🌈\n"
                                        + "🐣 /spawn: Spawn a random Pokemon for the user to try to catch\n"
                                        + "🦄 /pokemon: View your pokemon status\n"
                                        + "🔢 /my: Get your pokemon's stats by typing its number\n"
                                        + "🗡️ /battle: Start a battle with a random Pokémon, with a cost of only 5 coins\n"
                                        + "🍹 /feed: Choose the Pokemon you want to feed by typing its number\n\n"
                                        + "💫🧙‍♂️ Trainer 🧙‍♂️💫\n"
                                        + "💎 /daily: Reward the player with random coins upon completing their daily check-in\n"
                                        + "🛍️ /shop: A shop for trainer to purchase food\n"
                                        + "📊 /status: View your current status```"))
                .queue();
    }
}

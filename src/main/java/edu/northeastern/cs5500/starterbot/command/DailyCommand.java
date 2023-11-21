package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Random;
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
public class DailyCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "daily";

    // @Inject PokemonController pokemonController;

    // @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    public DailyCommand() {
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
                "Reward the player with random coins upon completing their daily check-in");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /daily");

        String trainerDiscordId = event.getMember().getId();
        trainerController.getTrainerForMemberId(trainerDiscordId);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(
                String.format("Hey <@%s>, ready for your daily adventure?", trainerDiscordId));
        embedBuilder.setDescription("Tap to discover today's reward!");
        embedBuilder.setColor(0x5CA266); // Same color as the check-in button
        // TODO: [zqy] Add the pic to the resources folder
        embedBuilder.setThumbnail(
                "https://img.freepik.com/premium-vector/money-stack-coins-dollar_464314-3776.jpg");

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder =
                messageCreateBuilder.addActionRow(
                        Button.success(getName() + ":checkin:" + trainerDiscordId, "Check In"));
        messageCreateBuilder = messageCreateBuilder.addEmbeds(embedBuilder.build());
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        event.reply("Enter the onButtonInteraction");
        String[] buttonIdParts = event.getButton().getId().split(":");
        // String action = buttonIdParts[1]; // "checkin"
        String initiateTrainerDiscordId = buttonIdParts[2];
        String trainerDiscordId = event.getMember().getId();

        LocalDate curCheckinDate = OffsetDateTime.now().toLocalDate();
        LocalDate lastCheckinDate =
                trainerController.getTrainerForMemberId(initiateTrainerDiscordId).getLastCheckIn();

        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

        // Handle the button interaction
        if (trainerDiscordId.equals(initiateTrainerDiscordId)
                && curCheckinDate.isAfter(lastCheckinDate)) {
            // Handle the 'Check In' action
            event.reply("Enter the succesfful case");
            Random random = new Random();
            Integer randomCoins = (random.nextInt(10) + 1) * 10; // 10, 20... 90, 100;
            trainerController.increaseTrainerBalance(trainerDiscordId, randomCoins);
            trainerController.updateLastCheckinDate(trainerDiscordId, curCheckinDate);
            Integer newBal = trainerController.getTrainerForMemberId(trainerDiscordId).getBalance();
            event.reply(
                            String.format(
                                    "Congratulations, <@%s>! Today, you've earned %d coins. Your current balance is %d coins!",
                                    trainerDiscordId, randomCoins, newBal))
                    .queue();
            event.getMessage()
                    .editMessageEmbeds(messageEmbed)
                    .setComponents()
                    .queue(); // disable button
        } else if (trainerDiscordId.equals(initiateTrainerDiscordId)
                && !curCheckinDate.isAfter(lastCheckinDate)) {
            event.reply(
                            String.format(
                                    "Oops, looks like you've already checked in today, <@%s>! Come back tomorrow for more exciting rewards and surprises!",
                                    trainerDiscordId))
                    .queue();
            event.getMessage()
                    .editMessageEmbeds(messageEmbed)
                    .setComponents()
                    .queue(); // disable button
        } else {
            event.reply(
                            String.format(
                                    "Oops, looks like you've already checked in today, <@%s>! Come back tomorrow for more exciting rewards!",
                                    trainerDiscordId))
                    .queue();
        }
    }
}

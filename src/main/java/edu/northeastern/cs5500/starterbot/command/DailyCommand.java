package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class DailyCommand implements SlashCommandHandler {

    static final String NAME = "daily";

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
        LocalDate curCheckinDate = OffsetDateTime.now().toLocalDate();
        Random random = new Random();
        Integer randomCoins = (random.nextInt(10) + 1) * 10; // 10, 20... 90, 100

        try {
            Integer resultBal =
                    trainerController.addDailyRewardsToTrainer(
                            trainerDiscordId, randomCoins, curCheckinDate);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(
                    String.format("Congratulations, you've earned %d coins today!", randomCoins));
            embedBuilder.setDescription("Come back tomorrow for more exciting rewards!");
            embedBuilder.setColor(0x5CA266); // Same color as the successful button
            embedBuilder.addField("Updated Balance", Integer.toString(resultBal), false);
            embedBuilder.setThumbnail( // TODO: [zqy] Add the pic to the resources folder
                    "https://img.freepik.com/premium-vector/money-stack-coins-dollar_464314-3776.jpg");

            MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
            messageCreateBuilder.addEmbeds(embedBuilder.build());
            event.reply(messageCreateBuilder.build()).queue();

        } catch (InvalidCheckinDayException e) {
            event.reply(
                            String.format(
                                    "Oops, looks like you've already checked in today, <@%s>!\nCome back tomorrow for more exciting rewards and surprises!",
                                    trainerDiscordId))
                    .queue();
        }
    }
}

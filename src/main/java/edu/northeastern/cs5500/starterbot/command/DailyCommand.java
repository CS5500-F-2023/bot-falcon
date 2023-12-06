package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
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
    @Inject PokemonController pokemonController;
    @Inject PokedexController pokedexController;

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
        Integer resultBal = 0; // Declare resultBal outside the try-catch block
        log.error("!!! random coins is" + randomCoins);

        try {
            resultBal =
                    trainerController.addDailyRewardsToTrainer(
                            trainerDiscordId, randomCoins, curCheckinDate);
            log.error("!!! result balance is" + resultBal);
        } catch (InvalidCheckinDayException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Oops, looks like you've already checked in today, <@");
            sb.append(trainerDiscordId);
            sb.append(">!\nCome back tomorrow for more exciting rewards and surprises!");
            event.reply(sb.toString()).queue();
            return;
        }

        EmbedBuilder greetingEmbed = createPokemonGreetingEmbed(trainerDiscordId);
        EmbedBuilder rewardEmbed = createDailyRewardEmbed(randomCoins, resultBal);
        String mention = String.format("<@%s>", trainerDiscordId);
        String content = mention + ", here is your daily update!";
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addEmbeds(greetingEmbed.build(), rewardEmbed.build());
        event.reply(content).setEmbeds(greetingEmbed.build(), rewardEmbed.build()).queue();
    }

    private EmbedBuilder createPokemonGreetingEmbed(String trainerDiscordId) {
        log.error("!!! createPokemonGreetingEmbed");
        List<Pokemon> inventory = trainerController.getTrainerPokemonInventory(trainerDiscordId);

        Pokemon pokemon;
        boolean isWild = false;

        if (inventory.isEmpty()) {
            pokemon = pokemonController.spawnRandonPokemon(); // TODO (zqy): in memory
            isWild = true;
        } else {
            int randomIndex = new Random().nextInt(inventory.size());
            pokemon = inventory.get(randomIndex);
        }

        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title =
                isWild
                        ? String.format("🌿 A wild %s greets you curiously 🌿", species.getName())
                        : String.format(
                                "🌟 Your %s bounds up to greet you with enthusiasm 🌟",
                                species.getName());
        embedBuilder.setTitle(title);
        log.error("!!! about to set Pokemon URL");
        embedBuilder.setImage(species.getImageUrl());
        embedBuilder.setColor(0x5CA266); // Same color as the successful button
        log.error("!!! set Pokemon URL");
        embedBuilder.setDescription(
                String.format("%s seems thrilled to see you!", species.getName()));
        return embedBuilder;
    }

    private EmbedBuilder createDailyRewardEmbed(Integer randomCoins, Integer resultBal) {
        log.error("!!! createDailyRewardEmbed");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(
                String.format("🎉 Hooray, you've earned %d coins today! 🎉", randomCoins));
        embedBuilder.setDescription("🚀 Come back tomorrow for more thrilling rewards! 🚀");
        embedBuilder.setColor(0x5CA266); // Same color as the successful button
        embedBuilder.addField("💰 New balance 💰", Integer.toString(resultBal) + " coins", false);
        log.error("!!! about to set coins URL");
        log.error("!!! set coins URL");
        return embedBuilder;
    }
}

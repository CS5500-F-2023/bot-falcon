package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.model.BotConstants;
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

@Slf4j
public class DailyCommand implements SlashCommandHandler {

    static final String NAME = "daily";
    static final int WIDTH = 14;

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
        Integer resultBal = 0;

        try {
            resultBal =
                    trainerController.addDailyRewardsToTrainer(
                            trainerDiscordId, randomCoins, curCheckinDate);
        } catch (InvalidCheckinDayException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Oops, looks like you've already checked in today, <@");
            sb.append(trainerDiscordId);
            sb.append(">!\nCome back tomorrow for more exciting rewards and surprises!");
            event.reply(sb.toString()).queue();
            return;
        }

        EmbedBuilder greetingEmbed = createPokemonGreetingEmbed(trainerDiscordId);
        EmbedBuilder rewardEmbed =
                createDailyRewardEmbed(resultBal - randomCoins, randomCoins, resultBal);
        event.replyEmbeds(greetingEmbed.build(), rewardEmbed.build()).queue();
    }

    private EmbedBuilder createPokemonGreetingEmbed(String trainerDiscordId) {
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
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title =
                isWild
                        ? String.format("🌿 A wild %s greets you curiously 🌿", species.getName())
                        : String.format(
                                "🌟 Your %s greets you with enthusiasm 🌟", species.getName());
        embedBuilder.setTitle(title);
        embedBuilder.setImage(species.getImageUrl());
        embedBuilder.setColor(BotConstants.COLOR_SUCCESS);
        embedBuilder.setDescription(
                String.format(
                        "```%s %s seems thrilled to see you!"
                                + " ".repeat(WIDTH - species.getName().length())
                                + "```",
                        species.getName(),
                        species.getRandomType().getEmoji()));
        return embedBuilder;
    }

    private EmbedBuilder createDailyRewardEmbed(
            Integer prevBal, Integer randomCoins, Integer newBal) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        int maxLen =
                Math.max(
                        Math.max(
                                Integer.toString(prevBal).length(),
                                Integer.toString(randomCoins).length()),
                        Integer.toString(newBal).length());

        String separator = "-".repeat(maxLen + 20);

        embedBuilder.setTitle(
                String.format("🥳 Hooray, you've earned %d coins today! 🥳", randomCoins));

        String statementFormat =
                "```\n"
                        + "🏦 Your Statement 🏦\n"
                        + "%s\n"
                        + ("Prev. Balance 💸:  %" + maxLen + "d\n")
                        + ("Coins Earned  🤑: +%" + (maxLen - 1) + "d\n")
                        + ("New Balance   💰:  %" + maxLen + "d\n\n")
                        + "More amazing rewards await you tomorrow 🎁  \n"
                        + "```";

        String statement = String.format(statementFormat, separator, prevBal, randomCoins, newBal);
        embedBuilder.setDescription(statement);
        embedBuilder.setColor(BotConstants.COLOR_SUCCESS);

        return embedBuilder;
    }
}

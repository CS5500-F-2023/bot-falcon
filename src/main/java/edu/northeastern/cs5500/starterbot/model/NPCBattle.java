package edu.northeastern.cs5500.starterbot.model;

import edu.northeastern.cs5500.starterbot.exception.InvalidBattleStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NPCBattle {

    private static final int REL_STRENGTH_ADDON = 2;

    private static final double EFFECTIVE_THRESHOLD = 1.0;

    private static final int FIXED_WIDTH = 30;
    private static final String BOARD_LINE = "----------------------------\n";

    private static final Integer BASE_COINS_FOR_WINNER = 20;
    private static final Integer FLOOR_COINS_FOR_WINNER = 7;
    private static final Integer CAP_COINS_FOR_WINNER = 60;

    private static final Integer BASE_EXP_FOR_WINNER = 40;
    private static final Integer FLOOR_EXP_FOR_WINNER = 5;
    private static final Integer CAP_EXP_FOR_WINNER = 80;

    private static final Integer BASE_EXP_FOR_LOSER = 15;
    private static final Integer FLOOR_EXP_FOR_LOSER = 8;
    private static final Integer CAP_EXP_FOR_LOSER = 25;

    String trDiscordId;
    String trPokemonIdStr;
    @Nonnull Trainer trainer;
    @Nonnull Pokemon trPokemon;
    Pokemon npcPokemon;
    PokemonSpecies trPokeSpecies;
    PokemonSpecies npcPokeSpecies;

    // Result related
    @Builder.Default boolean gameOver = false;
    @Builder.Default boolean trainerWins = false;
    @Builder.Default @Nonnegative int coinsEarned = 0;
    @Builder.Default int xpGained = 0;
    @Builder.Default boolean leveledUp = false;
    @Builder.Default boolean evolved = false;

    // Messages
    @Builder.Default List<ColoredMessage> messages = new ArrayList<>();

    public boolean getTrainerWins() {
        return this.trainerWins;
    }

    /** Key battle logic with updates of battle round msgs and battle result. */
    public void runBattle() {
        // Set current HP to max HP
        trPokemon.setCurrentHp(trPokemon.getHp());
        npcPokemon.setCurrentHp(npcPokemon.getHp());

        // Format start message
        this.messages.add(new ColoredMessage(formatStartMsg(), BotConstants.COLOR_WARNING));

        // Determine first mover
        Pokemon attackPokemon = this.getFirstAttacker();
        Pokemon defensePokemon = attackPokemon.equals(trPokemon) ? npcPokemon : trPokemon;

        while (!gameOver) {
            // Variables for ease of access
            PokemonSpecies aSpecies =
                    attackPokemon.equals(trPokemon) ? trPokeSpecies : npcPokeSpecies;
            PokemonSpecies dSpecies =
                    defensePokemon.equals(npcPokemon) ? npcPokeSpecies : trPokeSpecies;

            // Determine the random Pokémon types used for this round
            PokemonType aType = aSpecies.getRandomType();
            PokemonType dType = dSpecies.getRandomType();

            // Determine whether to use Physical or Special move
            boolean physical = new Random().nextBoolean();

            // Calculate damage and update HP
            int damage = Pokemon.getBaseDamage(attackPokemon, defensePokemon, physical);
            double multiplier = PokemonType.getMoveMultiplier(aType, dType);
            damage = (int) (damage * multiplier);
            damage += new Random().nextInt(5) - 3; // Random factor
            int newHP = defensePokemon.getCurrentHp() - damage;
            defensePokemon.setCurrentHp(newHP < 0 ? 0 : newHP);

            // Generate round message
            boolean isBot = attackPokemon.equals(npcPokemon);
            String s = formatRoundMsg(isBot, physical, aType, dType, multiplier, damage);
            this.messages.add(
                    new ColoredMessage(
                            s, isBot ? BotConstants.COLOR_NPC : BotConstants.COLOR_TRAINER));

            // Check if the game ends; if so, update final result
            if (trPokemon.getCurrentHp() <= 0 || npcPokemon.getCurrentHp() <= 0) {
                gameOver = true;
                if (npcPokemon.getCurrentHp() <= 0) trainerWins = true;
                String resultMessage;
                try { // Note: change to Trainer and Trainer Pokemon are in memory to format message
                    setCoinsEarned();
                    if (trainerWins) trainer.setBalance(trainer.getBalance() + coinsEarned);
                    setXpGained();
                    this.leveledUp = trPokemon.increaseExpPts(xpGained);
                    resultMessage = trainerWins ? buildVictoryMessage("") : buildDefeatMessage("");
                } catch (InvalidBattleStatusException e) {
                    resultMessage = "Error: " + e.getMessage();
                }
                this.messages.add(new ColoredMessage(resultMessage, BotConstants.COLOR_WARNING));
            } else { // Swith attacker and defenser
                Pokemon temp = attackPokemon;
                attackPokemon = defensePokemon;
                defensePokemon = temp;
            }
        }
    }

    /** Helper function determining the first mover in a random manner. */
    private Pokemon getFirstAttacker() {
        if (new Random().nextBoolean()) return trPokemon;
        else return npcPokemon;
    }

    /** Helper function to calculate and update the coins earned after the battle ends. */
    protected void setCoinsEarned() throws InvalidBattleStatusException {
        if (!gameOver) {
            throw new InvalidBattleStatusException("Set coinsEarn after battle ends.");
        }
        int relStrength = Pokemon.getRelStrength(trPokemon, npcPokemon);
        if (trainerWins) {
            coinsEarned = BASE_COINS_FOR_WINNER - relStrength * REL_STRENGTH_ADDON;
            if (coinsEarned < FLOOR_COINS_FOR_WINNER) coinsEarned = FLOOR_COINS_FOR_WINNER;
            if (coinsEarned > CAP_COINS_FOR_WINNER) coinsEarned = CAP_COINS_FOR_WINNER;
        }
    }

    /** Helper function to calculate and update the experience gained after the battle ends. */
    protected void setXpGained() throws InvalidBattleStatusException {
        if (!gameOver) {
            throw new InvalidBattleStatusException("Set coinsEarn after battle ends.");
        }
        int relStrength = Pokemon.getRelStrength(trPokemon, npcPokemon);
        if (trainerWins) {
            xpGained = (BASE_EXP_FOR_WINNER - relStrength * REL_STRENGTH_ADDON);
            if (xpGained < FLOOR_EXP_FOR_WINNER) xpGained = FLOOR_EXP_FOR_WINNER;
            if (xpGained > CAP_EXP_FOR_WINNER) xpGained = CAP_EXP_FOR_WINNER;
        } else {
            xpGained = (BASE_EXP_FOR_LOSER - relStrength * REL_STRENGTH_ADDON);
            if (xpGained < FLOOR_EXP_FOR_LOSER) xpGained = FLOOR_EXP_FOR_LOSER;
            if (xpGained > CAP_EXP_FOR_LOSER) xpGained = CAP_EXP_FOR_LOSER;
        }
    }

    /** ------------------------ Below are message formatters ------------------------ */

    /** Helper function to format battle start message. */
    private String formatStartMsg() {
        return "```" + "🥊 The battle begins! 🥊\n" + formatBarMsg() + "```";
    }

    /**
     * Helper function to build the battle round message
     *
     * @param isBot if attacking Pokemon is NPC Pokemon
     * @param physical if the move is a Physical Attack (or a Special Attack)
     * @param aType PokemonType for the attacking Pokemon
     * @param dType PokemonType for the defending Pokemon
     * @param multiplier multifilier due to PokemonTypes as double
     * @param damage final damage as int
     * @return a formmmated string
     */
    private String formatRoundMsg(
            boolean isBot,
            boolean physical,
            PokemonType aType,
            PokemonType dType,
            double multiplier,
            int damage) {
        PokemonSpecies aSpecies = isBot ? npcPokeSpecies : trPokeSpecies;
        PokemonSpecies dSpecies = isBot ? trPokeSpecies : npcPokeSpecies;
        StringBuilder builder = new StringBuilder();
        builder.append("```");
        builder.append(formatAttackMsg(isBot, physical, aSpecies.getName(), aType.getEmoji()));
        builder.append(
                formatDamageMsg(isBot, dSpecies.getName(), dType.getEmoji(), damage, multiplier));
        builder.append(formatBarMsg());
        builder.append("```");
        return builder.toString();
    }

    /** Sample: "🥊 Your Pikachu 🏙️ used Special Attack ✨" */
    private String formatAttackMsg(
            boolean attackerIsBot,
            boolean isPhysicalMove,
            String attackPokeName,
            String attackEmoji) {
        return String.format(
                "🥊 %s %s %s used %s Attack %s\n",
                (attackerIsBot ? "Bot's" : "Your"),
                attackPokeName,
                attackEmoji,
                (isPhysicalMove ? "Physical" : "Special"),
                (isPhysicalMove ? "💪" : "🔮"));
    }

    /** Sample: "🛡️ Bot's Charmander 🔥 took only 65 damag" */
    private String formatDamageMsg(
            boolean attackerIsBot,
            String defensePokeName,
            String defenseEmoji,
            int damage,
            double multiplier) {
        String effectiveness =
                multiplier < EFFECTIVE_THRESHOLD
                        ? "only "
                        : (multiplier == EFFECTIVE_THRESHOLD ? "" : "effective ");
        return String.format(
                "🛡️ %s %s %s took %s%d damage\n",
                (attackerIsBot ? "Your" : "Bot's"),
                defensePokeName,
                defenseEmoji,
                effectiveness,
                damage);
    }

    // Sample:
    // ----------------------------------------------
    // Your Pikachu     | HP: █████░░░░░░░░░░ 105/120
    // Bot's Charmander | HP: ██████████░░░░░   5/15
    // ----------------------------------------------
    private String formatBarMsg() {
        int nameLen =
                Math.max(
                        ("Your " + trPokeSpecies.getName()).length(),
                        ("Bot's " + npcPokeSpecies.getName()).length());
        String borderLine = "-".repeat(FIXED_WIDTH + nameLen) + "\n";
        return String.format(
                borderLine
                        + ("%-" + nameLen + "s | HP: %s %3d/%-3d\n")
                        + ("%-" + nameLen + "s | HP: %s %3d/%-3d\n")
                        + borderLine,
                String.format("Your %s", trPokeSpecies.getName()),
                trPokemon.generateHealthBar(),
                trPokemon.getCurrentHp(),
                trPokemon.getHp(),
                String.format("Bot's %s", npcPokeSpecies.getName()),
                npcPokemon.generateHealthBar(),
                npcPokemon.getCurrentHp(),
                npcPokemon.getHp());
    }

    // Sample
    // 💥🛡️💥 BATTLE CONCLUDED 💥🛡️💥
    //
    // 💔 Tough luck, your Spiritomb bravely faced the challenge!
    //
    // 🔥 Spiritomb's Gains 🔥
    // ----------------------------
    //    XP Earned    🌟 : +17
    //    Current XP   🏆 :  27
    //
    // 💸 Trainer's Expense 💸
    // ----------------------------
    //    Battle Cost 💸  : -5
    //    New Balance 💰  :  0
    //
    // 🌟 Every battle is a lesson. Your next victory awaits!
    public String buildVictoryMessage(String evolvedName) throws InvalidBattleStatusException {
        if (!gameOver) {
            throw new InvalidBattleStatusException("Build defeat message after game overs.");
        }
        StringBuilder builder = new StringBuilder();

        builder.append("🌟🏆🌟 VICTORY ACHIEVED! 🌟🏆🌟\n\n");
        builder.append("🎉 A splendid triumph, your ");
        builder.append(trPokeSpecies.getName()).append(" shines in glory!\n\n");

        builder.append("🔥 ").append(trPokeSpecies.getName()).append("'s Rewards 🔥\n");
        builder.append(BOARD_LINE);
        builder.append("   XP Spark     🌟 : +").append(xpGained).append("\n");
        builder.append("   Current XP   🏆 :  ").append(trPokemon.getExPoints()).append("\n");

        builder.append(buildStringuildLevelUpAndEvolvedMsg(evolvedName));
        builder.append("\n");

        builder.append("💰 Trainer's Bounty 💰\n");
        builder.append(BOARD_LINE);
        builder.append("   Coins Earned 🤑 : +").append(coinsEarned).append("\n");
        builder.append("   Battle Cost  💸 : -").append(BotConstants.COST_PER_BATTLE).append("\n");
        builder.append("   New Balance  💰 :  ").append(trainer.getBalance()).append("\n\n");

        builder.append("🌈 Celebrate this victory. The journey to greatness continues!\n");

        return "```" + builder.toString() + "```";
    }

    // Sample:
    // 🌟🏆🌟 VICTORY ACHIEVED! 🌟🏆🌟
    //
    // 🎉 A splendid triumph, your Torkoal shines in glory!
    //
    // 🔥 Torkoal's Rewards 🔥
    // ----------------------------
    //    XP Spark     🌟 : +36
    //    Current XP   🏆 :  46
    //
    // 💰 Trainer's Bounty 💰
    // ----------------------------
    //    Coins Earned 🤑 : +16
    //    Battle Cost  💸 : -5
    //    New Balance  💰 :  96
    //
    // 🌈 Celebrate this victory. The journey to greatness continues!
    public String buildDefeatMessage(String evolvedName) throws InvalidBattleStatusException {
        if (!gameOver) {
            throw new InvalidBattleStatusException("Build defeat message after game overs.");
        }
        StringBuilder builder = new StringBuilder();

        builder.append("💥🛡️💥 BATTLE CONCLUDED 💥🛡️💥\n\n");
        builder.append("💔 Tough luck, your ");
        builder.append(trPokeSpecies.getName()).append(" bravely met the challenge!\n\n");

        builder.append("🔥 ").append(trPokeSpecies.getName()).append("'s Gains 🔥\n");
        builder.append(BOARD_LINE);
        builder.append("   XP Earned    🌟 : +").append(xpGained).append("\n");
        builder.append("   Current XP   🏆 :  ").append(trPokemon.getExPoints()).append("\n");

        builder.append(buildStringuildLevelUpAndEvolvedMsg(evolvedName));
        builder.append("\n");

        builder.append("💸 Trainer's Expense 💸\n");
        builder.append(BOARD_LINE);
        builder.append("   Battle Cost 💸  : -").append(BotConstants.COST_PER_BATTLE).append("\n");
        builder.append("   New Balance 💰  :  ").append(trainer.getBalance()).append("\n\n");

        builder.append("🌟 Every battle is a lesson. Your next victory awaits!\n");

        return "```" + builder.toString() + "```";
    }

    /** Helper function to format message. */
    private String buildStringuildLevelUpAndEvolvedMsg(String evolvedName) {
        StringBuilder builder = new StringBuilder();
        if (leveledUp) {
            builder.append("   LEVEL UP to  📈 :  ");
            builder.append(trPokemon.getLevel()).append("\n");
        }
        if (evolved) {
            builder.append("   EVOLVE to    🚀 :  ");
            builder.append(evolvedName).append("\n");
            builder.append("\n📊 Use /pokemon or /my to check ");
            builder.append(evolvedName);
            builder.append("'s status!\n");
        }
        return builder.toString();
    }
}

package edu.northeastern.cs5500.starterbot.model;

import edu.northeastern.cs5500.starterbot.exception.InvalidBattleStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NPCBattle {

    private static final int DAMAGE_FLOOR = 8;
    private static final int BASE_LEVEL = 5;
    private static final int FIXED_WIDTH = 30;
    private static final int COST_PER_BATTLE = -5;
    private static final double LEVEL_MULTIPLIER_BASE = 0.1;
    private static final double EFFECTIVE_THRESHOLD = 1.0;
    private static final double DEFENSE_MULTIPLIER = 0.65;
    private static final String BOARD_LINE = "----------------------------\n";

    String trDiscordId;
    Trainer trainer;
    Pokemon trPokemon;
    Pokemon npcPokemon;
    PokemonSpecies trPokeSpecies;
    PokemonSpecies npcPokeSpecies;

    // Result related
    @Builder.Default boolean gameOver = false;
    @Builder.Default boolean trainerWins = false;
    @Builder.Default int coinsEarned = COST_PER_BATTLE;
    @Builder.Default int xpGained = 0;
    @Builder.Default String resultMessage = "";

    // Round related
    @Builder.Default List<String> battleRounds = new ArrayList<>();

    /** Key battle logic with updates of battle round msgs and battle result. */
    public void runBattle() {
        // Set current HP to max HP
        trPokemon.setCurrentHp(trPokemon.getHp());
        npcPokemon.setCurrentHp(npcPokemon.getHp());

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
            int damage = getBaseDamage(attackPokemon, defensePokemon, physical);
            double multiplier = PokemonType.getMoveMultiplier(aType, dType);
            damage = (int) (damage * multiplier * 2.0); // TODO: zqy: 2.0 is hard coded
            damage += new Random().nextInt(5) - 3; // TODO: zqy: hard coded
            int newHP = defensePokemon.getCurrentHp() - damage;
            defensePokemon.setCurrentHp(newHP < 0 ? 0 : newHP);

            // Generate round message
            boolean isBot = attackerIsBot(attackPokemon);
            String msg =
                    "```"
                            + formatAttackMsg(isBot, physical, aSpecies.getName(), aType.getEmoji())
                            + formatDamageMsg(
                                    isBot, dSpecies.getName(), dType.getEmoji(), damage, multiplier)
                            + formatBarMsg()
                            + "```";
            this.battleRecord.addBattleRoundInfo(msg);

            // Check if the game ends; if so, update final result
            if (trPokemon.getCurrentHp() <= 0) {
                this.updateFinalResult(false);
                gameOver = true;
            } else if (npcPokemon.getCurrentHp() <= 0) {
                this.updateFinalResult(true);
                gameOver = true;
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

    /** Helper function calculating the base damage depending on base damage. */
    protected static int getBaseDamage(Pokemon attacker, Pokemon defender, boolean isPhysicalMove) {
        double attack = isPhysicalMove ? attacker.getAttack() : attacker.getSpecialAttack();
        attack *= 1.0 + LEVEL_MULTIPLIER_BASE * (attacker.getLevel() - BASE_LEVEL);
        double defense = isPhysicalMove ? defender.getDefense() : defender.getSpecialDefense();
        defense *= 1.0 + LEVEL_MULTIPLIER_BASE * (defender.getLevel() - BASE_LEVEL);
        defense *= DEFENSE_MULTIPLIER; // so that attack is generally more effective
        return (attack - defense < DAMAGE_FLOOR) ? DAMAGE_FLOOR : (int) (attack - defense);
    }

    /** Helper function updating the result when the battle ends. */
    private void updateFinalResult(boolean trainerWins) {
        this.battleRecord.updateFinalResult(trPokemon, npcPokemon, trainerWins);
    }

    /** Helper function determining if the attacking Pokemon is NPC Pokemon. */
    private boolean attackerIsBot(Pokemon attacker) {
        return attacker.equals(npcPokemon);
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
                        ? "only"
                        : (multiplier == EFFECTIVE_THRESHOLD ? "effective" : "very effective");
        return String.format(
                "🛡️ %s %s %s took %s %d damage\n",
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

    // Sample:
    // 🌟🏆🌟 VICTORY ACHIEVED! 🌟🏆🌟
    //
    // 🎉 A splendid triumph, @ToastedAvo🥑! Pikachu shines in glory!
    //
    // 🔥 Pikachu's Rewards 🔥
    // ----------------------------
    //    XP Spark 🌟     : +40
    //    Current XP 🏆   :  50
    //    LEVEL UP 🚀 LV  :  6
    //
    // 💰 Trainer's Bounty 💰
    // ----------------------------
    //    Coins Earned 🪙 : +20
    //    New Balance 💸  :  185
    //
    // 🌈 Celebrate this victory! The journey to greatness continues, @ToastedAvo🥑 and Pikachu!
    //
    private String buildVictoryMessage(boolean leveledUp) throws InvalidBattleStatusException {
        if (!gameOver) {
            throw new InvalidBattleStatusException("Build defeat message after game overs.");
        }
        StringBuilder builder = new StringBuilder();

        builder.append("🌟🏆🌟 VICTORY ACHIEVED! 🌟🏆🌟\n\n");
        builder.append("🎉 A splendid triumph, <@").append(trDiscordId).append(">! ");
        builder.append(trPokeSpecies.getName()).append(" shines in glory!\n\n");

        builder.append("🔥 Pikachu's Rewards 🔥\n");
        builder.append(BOARD_LINE);
        builder.append("   XP Spark 🌟     : +").append(xpGained).append("\n");
        builder.append("   Current XP 🏆   :  ").append(trPokemon.getExPoints()).append("\n");

        if (leveledUp) {
            builder.append("   LEVEL UP 🚀 LV  :  ");
            builder.append(trPokemon.getLevel()).append("!\n");
        }
        builder.append("\n");

        builder.append("💰 Trainer's Bounty 💰\n");
        builder.append(BOARD_LINE);
        builder.append("   Coins Earned 🪙 : +").append(coinsEarned).append("\n");
        builder.append("   New Balance 💸  :  ").append(trainer.getBalance()).append("\n\n");

        builder.append("🌈 Celebrate this victory! The journey to greatness continues, <@")
                .append(trDiscordId)
                .append("> and ")
                .append(trPokeSpecies.getName())
                .append("!\n");

        return builder.toString();
    }

    //
    // 💥🛡️💥 BATTLE CONCLUDED 💥🛡️💥
    //
    // 💔 Tough luck, @ToastedAvo🥑. Pikachu bravely faced the challenge!
    //
    // 🔥 Pikachu's Gains 🔥
    // ----------------------------
    //    XP Earned 🌟    : +15
    //    Current XP 🏆   :  65
    //    LEVEL UP 🚀 LV  :  6
    //
    // 💸 Trainer's Expense 💸
    // ----------------------------
    //    Battle Cost 🪙  : -5
    //    New Balance 💰  :  180
    //
    // 🌟 Every battle is a lesson. @ToastedAvo🥑 and Pikachu, your next victory awaits!
    //
    private String buildDefeatMessage(boolean leveledUp) throws InvalidBattleStatusException {
        if (!gameOver) {
            throw new InvalidBattleStatusException("Build defeat message after game overs.");
        }
        StringBuilder builder = new StringBuilder();

        builder.append("💥🛡️💥 BATTLE CONCLUDED 💥🛡️💥\n\n");
        builder.append("💔 Tough luck, <@").append(trDiscordId).append(">. ");
        builder.append(trPokeSpecies.getName()).append(" bravely faced the challenge!\n\n");

        builder.append("🔥 Pikachu's Gains 🔥\n");
        builder.append(BOARD_LINE);
        builder.append("   XP Earned 🌟    : +").append(xpGained).append("\n");
        builder.append("   Current XP 🏆   :  ").append(trPokemon.getExPoints()).append("\n");

        if (leveledUp) {
            builder.append("   LEVEL UP 🚀 LV  :  ");
            builder.append(trPokemon.getLevel()).append("!\n");
        }
        builder.append("\n");

        builder.append("💸 Trainer's Expense 💸\n");
        builder.append(BOARD_LINE);
        builder.append("   Battle Cost 🪙  : -").append(-1 * coinsEarned).append("\n");
        builder.append("   New Balance 💰  :  ").append(trainer.getBalance()).append("\n\n");

        builder.append("🌟 Every battle is a lesson. <@")
                .append(trDiscordId)
                .append("> and ")
                .append(trPokeSpecies.getName())
                .append(", your next victory awaits!\n");

        return builder.toString();
    }
}

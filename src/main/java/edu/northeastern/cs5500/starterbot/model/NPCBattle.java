package edu.northeastern.cs5500.starterbot.model;

import java.util.Random;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NPCBattle {

    private static final int DAMAGE_FLOOR = 8;
    private static final int BASE_LEVEL = 5;
    private static final double LEVEL_MULTIPLIER_BASE = 0.1;
    private static final double EFFECTIVE_THRESHOLD = 1.0;
    private static final double DEFENSE_MULTIPLIER = 0.65;

    Pokemon trPokemon;
    Pokemon npcPokemon;
    PokemonSpecies trPokeSpecies;
    PokemonSpecies npcPokeSpecies;
    @Builder.Default boolean gameOver = Boolean.FALSE;
    @Builder.Default BattleRecord battleRecord = BattleRecord.builder().build();

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
                7 + Math.max(trPokeSpecies.getName().length(), npcPokeSpecies.getName().length());
        String borderLine = "-".repeat(29 + nameLen) + "\n";
        return String.format(
                borderLine
                        + ("%-" + nameLen + "s| HP: %s %3d/%-3d\n")
                        + ("%-" + nameLen + "s| HP: %s %3d/%-3d\n")
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
}

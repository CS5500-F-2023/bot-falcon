package edu.northeastern.cs5500.starterbot.model;

import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Builder
@Data
@AllArgsConstructor
public class Pokemon implements Model {

    // Level up constants
    private static final Integer DEFAULT_LEVEL = 5;
    private static final Integer DEFAULT_XP = 10;
    private static final Integer LEVEL_UP_THRESHOLD = 100;

    // Battle realted
    private static final double LEVEL_ADDON = 2.0;
    private static final double ATTACK_MULTIPLIER = 1.2;
    private static final double DEFENSE_MULTIPLIER = 0.8;
    private static final int FLOOR_DAMAGE = 8;

    // Formatted message related
    private static final Integer TOTAL_HEALTH_BARS = 15;

    @Nonnull @Builder.Default ObjectId id = new ObjectId();

    @Nonnull Integer pokedexNumber;

    @Builder.Default Integer level = DEFAULT_LEVEL;

    @Builder.Default Integer exPoints = DEFAULT_XP;

    @Nonnull Integer currentHp; // default: hp
    @Nonnull Integer hp;
    @Nonnull Integer attack;
    @Nonnull Integer defense;
    @Nonnull Integer specialAttack; // spAttack
    @Nonnull Integer specialDefense; // spDefense
    @Nonnull Integer speed;

    /**
     * Calculates the relative strength of two Pokémon.
     *
     * @param trPokemon The player's Pokémon
     * @param npcPokemon The NPC's Pokémon
     * @return a double > 1.0 if the Trainer Pokémon is stronger than the NPC's Pokémon strength,
     *     and a double < 1.0 if the Trainer Pokémon is weaker than the NPC's Pokémon strength
     */
    public static double getRelStrength(Pokemon trPokemon, Pokemon npcPokemon) {
        double roundsForTrToWin = calculateRounds(trPokemon, npcPokemon);
        double roundsForNpcToWin = calculateRounds(npcPokemon, trPokemon);
        return Math.round(roundsForNpcToWin / roundsForTrToWin * 100.0) / 100.0;
    }

    /** Helper function to determine num of rounds for the attacker to knock down the defender. */
    private static double calculateRounds(Pokemon attacker, Pokemon defender) {
        double physicalDamage = getBaseDamage(attacker, defender, true);
        double specialDamage = getBaseDamage(attacker, defender, false);
        double rounds = defender.getHp() / ((physicalDamage + specialDamage) / 2.0);
        return Math.round(rounds * 100.0) / 100.0;
    }

    /** Helper function calculating the base damage depending on base damage. */
    public static int getBaseDamage(Pokemon attacker, Pokemon defender, boolean physical) {
        double attack = (double) (physical ? attacker.getAttack() : attacker.getSpecialAttack());
        attack += LEVEL_ADDON * (attacker.getLevel() - DEFAULT_LEVEL);
        attack *= ATTACK_MULTIPLIER;
        double defense = (double) (physical ? defender.getDefense() : defender.getSpecialDefense());
        defense += LEVEL_ADDON * (defender.getLevel() - DEFAULT_LEVEL);
        defense *= DEFENSE_MULTIPLIER;
        return (attack - defense < FLOOR_DAMAGE) ? FLOOR_DAMAGE : (int) (attack - defense);
    }

    /**
     * Sets the experience points of the Pokémon and checks if it levels up.
     *
     * @param exPoints The new experience points total for the Pokémon
     * @return true if the Pokémon levels up as a result of the added EX points, otherwise false
     */
    public boolean setExPoints(int exPoints) {
        this.exPoints = exPoints;
        return this.levelUp();
    }

    /**
     * Handles the leveling up process of the Pokémon.
     *
     * @return True if the Pokémon has leveled up at least once during the process
     */
    private boolean levelUp() {
        boolean hasLeveledUP = false;
        while (exPoints >= LEVEL_UP_THRESHOLD) {
            this.level += 1;
            this.exPoints -= LEVEL_UP_THRESHOLD;
            hasLeveledUP = true;
        }
        return hasLeveledUP;
    }

    /**
     * Represents the ratio of the current HP to the maximum HP as a bar string.
     *
     * @return The string representation of a bar
     */
    public String generateHealthBar() {
        int filledBars = (int) Math.ceil(TOTAL_HEALTH_BARS * (this.currentHp * 1.0 / this.hp));
        return "█".repeat(filledBars) + "░".repeat(TOTAL_HEALTH_BARS - filledBars);
    }
}

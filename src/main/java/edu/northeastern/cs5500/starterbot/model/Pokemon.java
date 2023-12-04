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
    public static final Integer DEFAULT_LEVEL = 5;
    public static final Integer DEFAULT_XP = 10;
    public static final Integer LEVEL_UP_THRESHOLD = 100;

    // Battle realted
    private static final int LEVEL_ADDON = 3;
    private static final double ATTACK_MULTIPLIER = 1.2;
    private static final double DEFENSE_MULTIPLIER = 0.8;
    private static final int DAMAGE_FLOOR = 8;
    private static final int DAMAGE_CAP = 22;

    // Formatted message related
    private static final Integer TOTAL_HEALTH_BARS = 15;
    private static final Integer TOTAL_XP_BARS = 15;

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
        double defense = (double) (physical ? defender.getDefense() : defender.getSpecialDefense());
        double damage = attack * ATTACK_MULTIPLIER - defense * DEFENSE_MULTIPLIER;
        return (int) Math.max(Math.min(damage, DAMAGE_CAP), DAMAGE_FLOOR);
    }

    /**
     * Increases the experience points of a Pokemon by the specified amount.
     *
     * <p>It then checks if the Pokemon has gained enough experience to level up and if yes, will
     * update level and relevant stat automatically.
     *
     * @param increasedXPs The number of experience points to be added
     * @return true if the Pokémon levels up as a result of the added experience points, otherwise
     *     false
     */
    public boolean increaseExpPts(int increasedXPs) {
        setExPoints(this.exPoints + increasedXPs);
        return this.levelUp();
    }

    /** Private setter: Use `increaseExpPts()` instead. */
    private void setExPoints(int exPoints) {
        this.exPoints = exPoints;
    }

    /**
     * Handles the leveling up process of the Pokémon.
     *
     * @return True if the Pokémon has leveled up at least once during the process
     */
    private boolean levelUp() {
        int levelUpCount = 0;
        while (exPoints >= LEVEL_UP_THRESHOLD) {
            this.level += 1;
            this.exPoints -= LEVEL_UP_THRESHOLD;
            levelUpCount++;
        }
        updateStatDueToLevelUp(levelUpCount);
        return levelUpCount > 0;
    }

    /** Handles the update of stat due to level up. */
    private void updateStatDueToLevelUp(int level) {
        this.setHp(hp + LEVEL_ADDON * level);
        this.setAttack(attack + LEVEL_ADDON * level);
        this.setDefense(defense + LEVEL_ADDON * level);
        this.setSpecialAttack(specialAttack + LEVEL_ADDON * level);
        this.setSpecialDefense(specialDefense + LEVEL_ADDON * level);
        this.setSpeed(speed + LEVEL_ADDON * level);
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

    /**
     * Generates a progress bar for the experience points (XP) of a Pokemon.
     *
     * @return a string representing the XP progress bar
     */
    public String generateXpProgressBar() {
        double progressPercentage = (exPoints * 1.0 / LEVEL_UP_THRESHOLD) * 100.0;

        int filledBars = (int) Math.ceil(TOTAL_XP_BARS * (progressPercentage / 100.0));
        // TODO (hy) check if it's match to progress bar in feed
        return "█".repeat(filledBars) + "░".repeat(TOTAL_XP_BARS - filledBars);
    }
}

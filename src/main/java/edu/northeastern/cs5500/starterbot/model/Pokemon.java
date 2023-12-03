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

    // Weights for calculating Pokemon's strengths
    private static final double LEVEL_WEIGHT = 1.5;
    private static final double HP_WEIGHT = 2.0;
    private static final double ATTACK_DEFENSE_WEIGHT = 1.5;
    private static final double SPECIAL_WEIGHT = 1.5;
    private static final double SPEED_WEIGHT = 2.0;

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
     * @return The ratio of the player's Pokémon strength to the NPC's Pokémon strength
     */
    public static double getRelStrength(Pokemon trPokemon, Pokemon npcPokemon) {
        double trStrength = getStrength(trPokemon);
        double npcStrength = getStrength(npcPokemon);
        return Math.round(100.0 * trStrength / npcStrength) / 100.0;
    }

    /**
     * Helper function to get a Pokemon's strength
     *
     * @param pokemon
     * @return
     */
    private static double getStrength(Pokemon pokemon) {
        return LEVEL_WEIGHT * pokemon.level
                + HP_WEIGHT * pokemon.hp
                + ATTACK_DEFENSE_WEIGHT * (pokemon.attack + pokemon.defense)
                + SPECIAL_WEIGHT * (pokemon.specialAttack + pokemon.specialDefense)
                + SPEED_WEIGHT * pokemon.speed;
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

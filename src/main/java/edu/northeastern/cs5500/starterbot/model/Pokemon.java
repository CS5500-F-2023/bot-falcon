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

    private static final Integer DEFAULT_LEVEL = 5;
    private static final Integer DEFAULT_XP = 10;
    private static final Integer LEVEL_UP_THRESHOLD = 100;

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

    // Weights for different stats
    private static final double LEVEL_WEIGHT = 1.5;
    private static final double HP_WEIGHT = 2.0;
    private static final double ATTACK_DEFENSE_WEIGHT = 1.5;
    private static final double SPECIAL_WEIGHT = 1.5;
    private static final double SPEED_WEIGHT = 2.0;

    /**
     * Calculates the relative strength of two Pokémon.
     *
     * @param trPokemon The player's Pokémon
     * @param npcPokemon The NPC's Pokémon
     * @return The ratio of the player's Pokémon strength to the NPC's Pokémon strength
     */
    public static double getRelStrength(Pokemon trPokemon, Pokemon npcPokemon) {
        double trStrength =
                LEVEL_WEIGHT * trPokemon.level
                        + HP_WEIGHT * trPokemon.currentHp / (double) trPokemon.hp
                        + ATTACK_DEFENSE_WEIGHT * (trPokemon.attack + trPokemon.defense)
                        + SPECIAL_WEIGHT * (trPokemon.specialAttack + trPokemon.specialDefense)
                        + SPEED_WEIGHT * trPokemon.speed;

        double npcStrength =
                LEVEL_WEIGHT * npcPokemon.level
                        + HP_WEIGHT * npcPokemon.currentHp / (double) npcPokemon.hp
                        + ATTACK_DEFENSE_WEIGHT * (npcPokemon.attack + npcPokemon.defense)
                        + SPECIAL_WEIGHT * (npcPokemon.specialAttack + npcPokemon.specialDefense)
                        + SPEED_WEIGHT * npcPokemon.speed;

        return Math.round(100 * trStrength / npcStrength) / 100.0;
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

    public boolean canLevelUpWithAddedXP(int addedXP) {
        return this.exPoints + addedXP > LEVEL_UP_THRESHOLD;
    }
}

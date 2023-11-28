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
    @Nonnull @Builder.Default ObjectId id = new ObjectId();

    @Nonnull Integer pokedexNumber; // number

    @Nonnull @Builder.Default Integer level = 5;

    // TODO: zqy: add the EXP field

    @Nonnull Integer currentHp; // default: hp
    @Nonnull Integer hp;
    @Nonnull Integer attack;
    @Nonnull Integer defense;
    @Nonnull Integer specialAttack; // spAttack
    @Nonnull Integer specialDefense; // spDefense
    @Nonnull Integer speed;

    // Weights for different stats
    private static final double LEVEL_WEIGHT = 2.0;
    private static final double HP_WEIGHT = 1.0;
    private static final double ATTACK_DEFENSE_WEIGHT = 1.5;
    private static final double SPECIAL_WEIGHT = 1.2;
    private static final double SPEED_WEIGHT = 1.0;

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

        return trStrength / npcStrength;
    }
}

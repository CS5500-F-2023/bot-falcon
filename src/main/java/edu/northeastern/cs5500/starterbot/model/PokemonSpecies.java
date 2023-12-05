package edu.northeastern.cs5500.starterbot.model;

import java.util.Random;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PokemonSpecies { // Not implementing Model
    @Nonnull final Integer pokedexNumber; // number

    @Nonnull final String imageUrl; // spriteURL

    @Nonnull final String name; // speciesNames, "en"

    @Nonnull final String[] speciesTypes; // types

    @Nonnull final PokemonType[] types;

    /** Randomly selects a PokemonType from the available types. */
    public PokemonType getRandomType() {
        int idx = new Random().nextInt(types.length);
        return types[idx];
    }
}

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

    @Nonnull final PokemonType[] types; // TODO - to be removed, keep it just in case

    public PokemonType getRandomType() { // Use in battle
        int idx;
        if (speciesTypes.length == 1) idx = 0;
        else idx = new Random().nextInt(speciesTypes.length);
        return PokemonType.valueOf(speciesTypes[idx].toUpperCase());
    }
}

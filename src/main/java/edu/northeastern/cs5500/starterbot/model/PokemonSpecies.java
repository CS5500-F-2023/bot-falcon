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

    // Sample msg:
    // Species : 🐾 Rockruff
    // Types   : 🪨 Rock
    /**
     * Builds a string representation of the Pokemon Species's details.
     *
     * @param species The pokemon species.
     * @return A string containing pokemon species and types.
     */
    public String buildSpeciesDetails(PokemonSpecies species) {
        String typeString = String.join(", ", species.getSpeciesTypes());
        StringBuilder speciesDetailBuilder = new StringBuilder();
        speciesDetailBuilder.append("Species : 🐾 ").append(species.getName()).append("\n");
        speciesDetailBuilder.append("Types   : ").append(typeString).append("\n");
        return speciesDetailBuilder.toString();
    }
}

package edu.northeastern.cs5500.starterbot.model;

import java.awt.Color;
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

    /** Return the representitive color for a pokemon species. */
    public Integer getSpeciesColor() {
        if (types.length == 1) {
            return types[0].getColor();
        }
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int count = types.length;

        for (int i = 0; i < count; i++) {
            Color color = new Color(types[i].getColor());
            totalRed += color.getRed();
            totalGreen += color.getGreen();
            totalBlue += color.getBlue();
        }

        int red = totalRed / count;
        int green = totalGreen / count;
        int blue = totalBlue / count;

        Color newColor = new Color(red, green, blue);
        return newColor.getRed();
    }
}

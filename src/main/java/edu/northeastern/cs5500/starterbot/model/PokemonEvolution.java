package edu.northeastern.cs5500.starterbot.model;

import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Represents Pokemon evolution data from JSON resource file. */
@Data
@AllArgsConstructor
public class PokemonEvolution {
    @Nonnull String evolutionFrom;
    @Nonnull String evolutionTo;
}

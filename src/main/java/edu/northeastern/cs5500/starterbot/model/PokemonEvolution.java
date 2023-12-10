package edu.northeastern.cs5500.starterbot.model;

import java.util.List;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Represents Pokemon evolution data from JSON resource file. */
@Data
@AllArgsConstructor
public class PokemonEvolution {

    @Nonnull private String evolutionFrom;

    @Nonnull private String evolutionTo;

    @Nonnull private List<String> prev;
}

package edu.northeastern.cs5500.starterbot.model;

import com.mongodb.lang.NonNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PokemonSpecies { // Not implementing Model
    @NonNull final Integer pokedexNumber;

    @NonNull final String imageUrl;

    @NonNull final String name;

    @NonNull final PokemonType[] types;

    MoveEffectiveness getEffectiveness(PokemonType attackType) {
        return PokemonType.getEffectiveness(attackType, types);
    }
}

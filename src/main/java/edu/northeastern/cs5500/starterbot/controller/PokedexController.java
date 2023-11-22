package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies.PokemonSpeciesBuilder;
import edu.northeastern.cs5500.starterbot.model.PokemonType;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PokedexController {

    @Inject
    PokedexController() {
        // No args as we are not saving anythin
        // empty and defined for Dragger
    }

    @Nonnull
    public PokemonSpecies getePokemonSpeciesByNumber(int pokedexNumber) {
        // Normally load data from json, etc.
        // Here is the quick and dirty solution
        PokemonSpeciesBuilder builder = PokemonSpecies.builder();
        builder.pokedexNumber(pokedexNumber);

        switch (pokedexNumber) {
            case 1: // Bulbasaur
                builder.name("Bulbasaur");
                builder.types(PokemonType.getSingleTypeArray(PokemonType.GRASS));
                builder.imageUrl("https://placehold.co/256x256/green/white.png?text=Bulbasaur");
                break;
            case 4: // Charmander
                builder.name("Charmander");
                builder.types(PokemonType.getSingleTypeArray(PokemonType.FIRE));
                builder.imageUrl("https://placehold.co/256x256/red/white.png?text=Charmander");
                break;
            case 7: // Squirtle
                builder.name("Squirtle");
                builder.types(PokemonType.getSingleTypeArray(PokemonType.WATER));
                builder.imageUrl("https://placehold.co/256x256/blue/white.png?text=Squirtle");
                break;
            case 19: // Rattata
                builder.name("Rattata");
                builder.types(PokemonType.getSingleTypeArray(PokemonType.NORMAL));
                builder.imageUrl("https://placehold.co/256x256/grey/white.png?text=Rattata");
                break;
            default:
                throw new IllegalStateException();
        }
        return Objects.requireNonNull(builder.build());
    }

    /**
     * Builds a string representation of the Pokemon Species's details based on its pokedex number.
     *
     * @param pokedexNumber The index of the pokemon species.
     * @return A string containing pokemon species and types.
     */
    public String buildSpeciesDetails(int pokedexNumber) {
        PokemonSpecies species = this.getePokemonSpeciesByNumber(pokedexNumber);
        String typeString = PokemonType.getTypeString(species.getTypes());
        return String.format("Species: %s\nTypes: %s\n", species.getName(), typeString);
    }
}

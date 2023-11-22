package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies.PokemonSpeciesBuilder;
import edu.northeastern.cs5500.starterbot.model.PokemonType;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PokedexController {

    @Inject PokemonDataService pokemonDataService;

    @Inject
    PokedexController() {
        // No args as we are not saving anythin
        // empty and defined for Dragger
    }

    /**
     * Retrieves the Pokemon species information based on the given Pokedex number from Pokemon
     * data.
     *
     * @param pokedexNumber The Pokedex number of the Pokemon species.
     * @return The Pokemon species information.
     */
    public PokemonSpecies getPokemonSpeciesByPokedex(int pokedexNumber) {
        PokemonSpeciesBuilder builder = PokemonSpecies.builder();
        builder.pokedexNumber(pokedexNumber);
        List<PokemonData> pokemonDatas = pokemonDataService.getPokemonDataList();

        // find pokedex in the pokemon data list
        for (PokemonData data : pokemonDatas) {
            if (data.getNumber().equals(pokedexNumber)) {
                builder.name(data.getSpeciesNames().get("en"));
                builder.imageUrl(data.getSpriteURL());
                builder.speciesTypes(data.getTypes());
                builder.types(PokemonType.getSingleTypeArray(PokemonType.NORMAL)); // placeholder
                break;
            }
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
        PokemonSpecies species = this.getPokemonSpeciesByPokedex(pokedexNumber);
        String typeString = String.join(", ", species.getSpeciesTypes());
        return String.format("Species: %s\nTypes: %s\n", species.getName(), typeString);
    }
}

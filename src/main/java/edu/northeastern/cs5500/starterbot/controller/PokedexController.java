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

    List<PokemonData> pokemonDataList;

    @Inject
    PokedexController(PokemonDataService pokemonDataService) {
        this.pokemonDataService = pokemonDataService;
    }

    /**
     * Retrieves the Pokemon species info based on the given Pokedex number from Pokemon data.
     *
     * @param pokedexNumber The Pokedex number of the Pokemon species.
     * @return The Pokemon species information.
     * @throws FieldNotValidException
     */
    public PokemonSpecies getPokemonSpeciesByPokedex(int pokedexNumber) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        PokemonSpeciesBuilder builder = PokemonSpecies.builder();
        builder.pokedexNumber(pokedexNumber);

        // find pokedex in the pokemon data list
        PokemonData data = this.pokemonDataList.get(pokedexNumber);
        builder.name(data.getSpeciesNames().get("en"));
        builder.imageUrl(data.getSpriteURL());
        builder.speciesTypes(data.getTypes());
        builder.types(PokemonType.getSingleTypeArray(PokemonType.NORMAL)); // placeholder
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

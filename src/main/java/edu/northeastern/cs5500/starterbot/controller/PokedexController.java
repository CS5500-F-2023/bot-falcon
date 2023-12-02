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

    // TODO (h): the getPokemonSpeciesByPokedex is not accurate, should be getPokemonSpeciesByIndex
    /**
     * Retrieves the Pokemon species info based on the given Pokedex number from Pokemon data.
     *
     * @param listIndex The index of the data list.
     * @return The Pokemon species information.
     */
    public PokemonSpecies getPokemonSpeciesByPokedex(int listIndex) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        PokemonSpeciesBuilder builder = PokemonSpecies.builder();

        // find pokemon in the pokemon data list base on list index
        PokemonData data = this.pokemonDataList.get(listIndex);
        // build pokedex base on actual pokedex from the Number field
        builder.pokedexNumber(data.getNumber());
        builder.name(data.getSpeciesNames().get("en"));
        builder.imageUrl(data.getSpriteURL());
        builder.speciesTypes(data.getTypes());
        builder.types(
                PokemonType.getSingleTypeArray(
                        PokemonType.NORMAL)); // TODO placeholder leftover from demo code
        return Objects.requireNonNull(builder.build());
    }

    /**
     * Builds a string representation of the Pokemon Species's details based on its pokedex number.
     *
     * @param listIndex The index of the pokemon species.
     * @return A string containing pokemon species and types.
     */
    public String buildSpeciesDetails(int listIndex) {
        PokemonSpecies species = this.getPokemonSpeciesByPokedex(listIndex);
        String typeString = String.join(", ", species.getSpeciesTypes());
        return String.format(
                "Species: %s\nTypes: %s\nPokedex: %d\n",
                species.getName(), typeString, species.getPokedexNumber());
    }
}

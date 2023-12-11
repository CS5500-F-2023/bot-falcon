package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies.PokemonSpeciesBuilder;
import edu.northeastern.cs5500.starterbot.model.PokemonType;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
     * Retrieves the Pokemon species info based on the given index number from Pokemon data list.
     *
     * @param listIndex The index of the data list.
     * @return The Pokemon species information.
     */
    private PokemonSpecies getPokemonSpeciesByPokedex(int listIndex) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        PokemonSpeciesBuilder builder = PokemonSpecies.builder();

        // find pokemon in the pokemon data list base on list index
        PokemonData data = this.pokemonDataList.get(listIndex);
        // build pokedex base on actual pokedex from the Number field
        PokemonSpecies species = buildPokemonSpecies(data);
        return Objects.requireNonNull(species);
    }

    /**
     * Retrieves the Pokemon species based on the given Pokedex number.
     *
     * @param pokedex The Pokedex number of the Pokemon species to retrieve.
     * @return The Pokemon species corresponding to the given Pokedex number.
     * @throws RuntimeException if the species is not found for the given Pokedex number.
     */
    public PokemonSpecies getPokemonSpeciesByREALPokedex(int pokedex) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        Optional<PokemonSpecies> optionalSpecies =
                pokemonDataList.stream()
                        .filter(data -> data.getNumber().equals(pokedex))
                        .map(this::buildPokemonSpecies)
                        .findFirst();
        // todo add customized exception
        return optionalSpecies.orElseThrow(
                () -> new RuntimeException("Species not found for Pokedex number: " + pokedex));
    }

    /**
     * Builds a PokemonSpecies object based on the provided PokemonData.
     *
     * @param data The PokemonData object containing the data for the Pokemon.
     * @return The constructed PokemonSpecies object.
     */
    private PokemonSpecies buildPokemonSpecies(PokemonData data) {
        String[] types = PokemonType.buildTypesWithEmoji(data.getTypes());
        PokemonType[] pokemonTypes = PokemonType.buildPokemonTypes(data.getTypes());
        int pokedex = data.getNumber();
        String formatSpecifier = (pokedex < 1000) ? "%03d" : "%d";
        String formattedNumber = String.format(formatSpecifier, pokedex);
        return PokemonSpecies.builder()
                .pokedexNumber(pokedex)
                .name(data.getSpeciesNames().get("en"))
                .imageUrl(
                        String.format(
                                "http://www.serebii.net/pokemongo/pokemon/%s.png", formattedNumber))
                .speciesTypes(types)
                .types(pokemonTypes)
                .build();
    }


    // todo(yhr): remove this method after current PRs merged and no conflicts
    /**
     * Builds a string representation of the Pokemon Species's details based on its pokedex number.
     *
     * @param pokedex The index of the pokemon species.
     * @return A string containing pokemon species and types.
     */
    public String buildSpeciesDetails(int pokedex) {
        PokemonSpecies species = this.getPokemonSpeciesByREALPokedex(pokedex);
        String typeString = String.join(", ", species.getSpeciesTypes());
        StringBuilder speciesDetailBuilder = new StringBuilder();
        speciesDetailBuilder.append("Species : 🐾 ").append(species.getName()).append("\n");
        speciesDetailBuilder.append("Types   : ").append(typeString).append("\n");
        return speciesDetailBuilder.toString();
    }
}

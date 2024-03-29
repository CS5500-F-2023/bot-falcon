package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.PokemonType;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import java.util.List;
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
}

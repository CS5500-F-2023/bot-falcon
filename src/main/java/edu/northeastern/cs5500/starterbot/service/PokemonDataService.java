package edu.northeastern.cs5500.starterbot.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

/** This class represents a service for retrieving Pokemon data. */
@Singleton
public class PokemonDataService {
    private static String POKEMON_DATA_FILE_NAME = "src/main/resources/pokemon.json";

    private List<PokemonData> pokemonDataList;

    /** Constructs a new PokemonDataService and loads the Pokemon data. */
    @Inject
    public PokemonDataService() {
        loadPokemonData();
    }

    public PokemonDataService(String path) {
        loadPokemonDataWithPath(path);
    }

    /** Loads the Pokemon data from a JSON file. */
    private void loadPokemonData() {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(POKEMON_DATA_FILE_NAME);
            pokemonDataList =
                    gson.fromJson(reader, new TypeToken<List<PokemonData>>() {}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadPokemonDataWithPath(String p) {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(p);
            pokemonDataList =
                    gson.fromJson(reader, new TypeToken<List<PokemonData>>() {}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the list of Pokemon data.
     *
     * @return the list of Pokemon data
     */
    public List<PokemonData> getPokemonDataList() {
        return pokemonDataList.stream()
                .filter(
                        pokemonData ->
                                !pokemonData.hasNullFields() && !pokemonData.hasEmptyFields())
                .collect(Collectors.toList());
    }
}

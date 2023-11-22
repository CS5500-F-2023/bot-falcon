package edu.northeastern.cs5500.starterbot.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import javax.inject.Inject;

/** This class represents a service for retrieving Pokemon data. */
public class PokemonDataService {
    //TODO update to real resource file
    private static final String POKEMON_DATA_FILE_NAME = "src/main/resources/pokeDataTest.json";

    private List<PokemonData> pokemonDataList;

    /** Constructs a new PokemonDataService and loads the Pokemon data. */
    @Inject
    public PokemonDataService() {
        loadPokemonData();
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

    /**
     * Returns the list of Pokemon data.
     *
     * @return the list of Pokemon data
     */
    public List<PokemonData> getPokemonDataList() {
        return pokemonDataList;
    }
}

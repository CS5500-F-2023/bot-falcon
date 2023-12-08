package edu.northeastern.cs5500.starterbot.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.northeastern.cs5500.starterbot.model.PokemonEvolution;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/** This class represents a service for retrieving Pokemon Evolution data. */
@Singleton
public class PokemonEvolutionService {
    private static String POKEMON_DATA_FILE_NAME = "src/main/resources/evolution-chain.json";

    private Map<String, PokemonEvolution> pokemonEvolutionMap = new HashMap<>();

    /** Constructs a new PokemonEvolutionService and loads the Pokemon Evolution data. */
    @Inject
    public PokemonEvolutionService() {
        loadPokemonEvolution();
    }

    /** For testing purpose */
    public PokemonEvolutionService(String path) {
        loadPokemonEvolution(path);
    }

    /** Loads the Pokemon data from a JSON file. */
    private void loadPokemonEvolution() {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(POKEMON_DATA_FILE_NAME);
            pokemonEvolutionMap = gson.fromJson(reader, new TypeToken<Map<String, PokemonEvolution>>() {}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /** For testing purpose */
    private void loadPokemonEvolution(String p) {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(p);
            pokemonEvolutionMap = gson.fromJson(reader, new TypeToken<Map<String, PokemonEvolution>>() {}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns map of valid Pokemon evolution data.
     *
     * @return the map of Pokemon evolution data, with species name as key.
     */
    public Map<String, PokemonEvolution> getPokemonEvolutionMap() {
        return pokemonEvolutionMap;
    }
}

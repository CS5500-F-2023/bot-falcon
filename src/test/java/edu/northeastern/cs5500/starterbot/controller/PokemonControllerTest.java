package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import org.junit.jupiter.api.Test;

public class PokemonControllerTest {

    private PokemonController getPokemonController() {
        PokemonDataService pokemonDataService = new PokemonDataService();
        PokemonController pokemonController =
                new PokemonController(new InMemoryRepository<>(), pokemonDataService);
        return pokemonController;
    }

    @Test
    public void testPokemonDataListInitialization() {
        PokemonController pokemonController = getPokemonController();
        Pokemon p = pokemonController.spawnRandonPokemon();
        assertThat(p).isNotNull();
    }
}

package edu.northeastern.cs5500.starterbot.service;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.PokemonEvolution;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class PokemonEvolutionServiceTest {
    private PokemonEvolutionService pokemonEvolutionService;
    private Map<String, PokemonEvolution> pokemonEvolutionMap = new HashMap<>();

    @Before
    public void setUp() {
        pokemonEvolutionService =
                new PokemonEvolutionService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chainTest.json");
        pokemonEvolutionMap = pokemonEvolutionService.getPokemonEvolutionMap();
    }

    @Test
    public void testLoadJsonSuccessfully() {
        assertThat(pokemonEvolutionMap).isNotEmpty();
    }
}

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
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chain-Test.json");
        pokemonEvolutionMap = pokemonEvolutionService.getPokemonEvolutionMap();
    }

    @Test
    public void testLoadJsonSuccessfully() {
        assertThat(pokemonEvolutionMap.size()).isEqualTo(3);
    }

    @Test
    public void testContainsDesiredDate() {
        assertThat(pokemonEvolutionMap.containsKey("Bulbasaur")).isTrue();
        assertThat(pokemonEvolutionMap.get("Bulbasaur").getEvolutionFrom()).isEqualTo("");
        assertThat(pokemonEvolutionMap.get("Bulbasaur").getEvolutionTo()).isEqualTo("Ivysaur");
        assertThat(pokemonEvolutionMap.get("Bulbasaur").getPrev()).isEmpty();
    }
}

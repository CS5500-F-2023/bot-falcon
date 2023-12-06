package edu.northeastern.cs5500.starterbot.service;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.PokemonEvolution;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PokemonEvolutionServiceTest {
    private PokemonEvolutionService pokemonEvolutionService;
    private List<PokemonEvolution> pokemonEvolutions;

    @Before
    public void setUp() {
        pokemonEvolutionService =
                new PokemonEvolutionService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chainTest.json");
        pokemonEvolutions = pokemonEvolutionService.getPokemonEvolutionList();
    }

    @Test
    public void testLoadJsonSuccessfully() {
        for (PokemonEvolution pe : pokemonEvolutions) {
            System.out.println(pe.getEvolutionFrom() + " --> " + pe.getEvolutionTo());
        }
        assertThat(pokemonEvolutions).isNotEmpty();
    }
}

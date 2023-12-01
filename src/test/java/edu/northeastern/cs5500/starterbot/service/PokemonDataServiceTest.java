package edu.northeastern.cs5500.starterbot.service;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.PokemonData;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PokemonDataServiceTest {

    private PokemonDataService pokemonDataService;

    private List<PokemonData> pokemonDataList;

    @Before
    public void setUp() {
        // Manually instantiate PokemonDataService and inject it
        pokemonDataService = new PokemonDataService();
        // Load data for testing
        pokemonDataList = pokemonDataService.getPokemonDataList();
    }

    @Test
    public void testPokemonDataListAfterConstruct() {
        assertThat(pokemonDataList).isNotEmpty();
    }

    @Test
    public void testPokemonDataListSkipInvalidData() {
        PokemonDataService pokemonDataService2 =
                new PokemonDataService("src/main/resources/pokeDataTest.json");
        List<PokemonData> pokemonDataList2 = pokemonDataService2.getPokemonDataList();
        assertThat(pokemonDataList2.size()).isEqualTo(16);
    }
}

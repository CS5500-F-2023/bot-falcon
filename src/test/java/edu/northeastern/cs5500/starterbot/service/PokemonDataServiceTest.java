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

    // test skip invalid data during loading data from json
    @Test
    public void testPokemonfileContainsNull() {
        // imageUrl, number, speciesName, types
        PokemonDataService pokemonDataService1 =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/AllNullPokeDataTest.json");
        List<PokemonData> pokemonDataList1 = pokemonDataService1.getPokemonDataList();
        assertThat(pokemonDataList1.size()).isEqualTo(1);
    }

    @Test
    public void testPokemonContainsEmpty() {
        // speciesName, imageUrl, types
        PokemonDataService pokemonDataService2 =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/AllEmptyPokeDataTest.json");
        List<PokemonData> pokemonDataList2 = pokemonDataService2.getPokemonDataList();
        assertThat(pokemonDataList2.size()).isEqualTo(1);
    }

    @Test
    public void testPokemonContainsNullStats() {
        // hp
        PokemonDataService pokemonDataService3 =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/statsNullPokeDataTest.json");
        List<PokemonData> pokemonDataList3 = pokemonDataService3.getPokemonDataList();
        assertThat(pokemonDataList3.size()).isEqualTo(1);
    }
}

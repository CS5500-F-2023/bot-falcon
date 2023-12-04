package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import org.junit.jupiter.api.Test;

public class PokemonControllerTest {

    private PokemonController getPokemonController() {
        // mock injection
        PokemonDataService pokemonDataService =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/pokeDataTest_2.json");
        PokedexController pokedexController = new PokedexController(pokemonDataService);
        PokemonController pokemonController =
                new PokemonController(new InMemoryRepository<>(), pokemonDataService);

        pokemonController.pokedexController = pokedexController;
        pokedexController.pokemonDataList = pokemonDataService.getPokemonDataList();

        return pokemonController;
    }

    @Test
    public void testPokemonDataListInitialization() {
        PokemonController pokemonController = getPokemonController();
        Pokemon p = pokemonController.spawnRandonPokemon();
        assertThat(p).isNotNull();
    }

    @Test
    public void mockPokemonDetailString() {
        String attack = String.format("%s  : 🛡️ Phys. %-3d | 🔮 Sp. %-3d\n", "Attack", 1, 1);
        String defense = String.format("%s : 🛡️ Phys. %-3d | 🛡️ Sp. %-3d\n", "Defense", 1, 1);
        // Attack       : 🛡️ Phys. 10  | 🔮 Sp. 11
        // Defense      : 🛡️ Phys. 10  | 🛡️ Sp. 11
        // System.out.println(attack + defense);
    }

    @Test
    public void mockPokemonDetailString2() {
        String attack =
                String.format("%s             : 🛡️ Phys. %-3d | 🔮 Sp. %-3d\n", "Attack", 1, 11);
        String defense =
                String.format("%s            : 🛡️ Phys. %-3d | 🛡️ Sp. %-3d\n", "Defense", 10, 11);
        // Attack             : 🛡️ Phys. 1   | 🔮 Sp. 11
        // Defense            : 🛡️ Phys. 10  | 🛡️ Sp. 11
        // System.out.println(attack + defense);
    }

    @Test
    public void mockPokemonDetailString3() {
        String attack =
                String.format("%s             : 🛡️ Phys. %-3d | 🔮 Sp. %-3d\n", "Attack", 111, 11);
        String defense =
                String.format("%s            : 🛡️ Phys. %-3d | 🛡️ Sp. %-3d\n", "Defense", 10, 11);
        // Attack             : 🛡️ Phys. 111  | 🔮 Sp. 11
        // Defense            : 🛡️ Phys. 10   | 🛡️ Sp. 11
        // System.out.println(attack + defense);
    }
}

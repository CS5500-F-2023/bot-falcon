package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import edu.northeastern.cs5500.starterbot.service.PokemonEvolutionService;
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
        PokemonEvolutionService pokemonEvolutionService =
                new PokemonEvolutionService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chain-Test.json");

        // assign
        pokemonController.pokedexController = pokedexController;
        pokemonController.pokemonEvolutionService = pokemonEvolutionService;

        return pokemonController;
    }

    @Test
    public void testPokemonDataListInitialization() {
        PokemonController pokemonController = getPokemonController();
        Pokemon p = pokemonController.spawnRandonPokemon();
        assertThat(p).isNotNull();
    }

    @Test
    public void buildPrimitivePokemon() {
        PokemonController pokemonController = getPokemonController();
        pokemonController.pokemonDataList =
                pokemonController.pokemonDataService.getPokemonDataList();
        PokemonData bulData = pokemonController.pokemonDataList.get(0);
        Pokemon bul = pokemonController.buildPokemon(bulData);
        assertThat(bul.getLevel()).isEqualTo(Pokemon.DEFAULT_LEVEL);
    }

    @Test
    public void buildEvolvedPokemon() {
        PokemonController pokemonController = getPokemonController();
        pokemonController.pokemonDataList =
                pokemonController.pokemonDataService.getPokemonDataList();
        PokemonData ivyData = pokemonController.pokemonDataList.get(1);
        Pokemon ivy = pokemonController.buildPokemon(ivyData);
        assertThat(ivy.getLevel()).isEqualTo(10);
    }

    @Test
    public void buildEvolvedPokemon2() {
        PokemonController pokemonController = getPokemonController();
        pokemonController.pokemonDataList =
                pokemonController.pokemonDataService.getPokemonDataList();
        PokemonData venData = pokemonController.pokemonDataList.get(2);
        Pokemon ven = pokemonController.buildPokemon(venData);
        assertThat(ven.getLevel()).isEqualTo(15);
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

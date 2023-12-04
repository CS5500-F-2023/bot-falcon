package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
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
        PokemonEvolutionService pokemonEvolutionService =
                new PokemonEvolutionService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chainTest.json");
        PokemonController pokemonController =
                new PokemonController(new InMemoryRepository<>(), pokemonDataService);

        pokemonController.pokemonEvolutionService = pokemonEvolutionService;
        pokemonController.pokedexController = pokedexController;
        pokemonController.pokemonEvolutionList = pokemonEvolutionService.getPokemonEvolutionList();
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

    @Test
    public void testIsEvolved() {
        Pokemon pokemon =
                Pokemon.builder()
                        .pokedexNumber(1)
                        .level(5)
                        .currentHp(45)
                        .hp(45)
                        .attack(49)
                        .defense(49)
                        .specialAttack(65)
                        .specialDefense(65)
                        .speed(45)
                        .build();

        PokemonController pokemonController = getPokemonController();
        pokemonController.pokemonRepository.add(pokemon);
        boolean isEvolved = pokemonController.evolvePokemon(pokemon.getId().toString());
        assertThat(isEvolved).isTrue();
        assertThat(pokemon.getEvolvedFrom()).isEqualTo("Bulbasaur");

        // should evolve to Ivysaur
        assertThat(pokemon.getPokedexNumber()).isEqualTo(2);
    }
}

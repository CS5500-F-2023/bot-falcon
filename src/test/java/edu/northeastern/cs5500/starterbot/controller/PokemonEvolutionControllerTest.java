package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import edu.northeastern.cs5500.starterbot.service.PokemonEvolutionService;
import org.junit.Test;

public class PokemonEvolutionControllerTest {

    private PokemonEvolutionController getPokemonEvolutionController() {

        /** Mock Injection */
        PokemonEvolutionController pokemonEvolutionController = new PokemonEvolutionController();
        // pokemon data service
        PokemonDataService pokemonDataService =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/pokeDataTest_2.json");
        // pokemon evolution service
        PokemonEvolutionService pokemonEvolutionService =
                new PokemonEvolutionService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chainTest.json");
        // pokedex controller
        PokedexController pokedexController = new PokedexController(pokemonDataService);
        // pokemon controller
        PokemonController pokemonController =
                new PokemonController(new InMemoryRepository<>(), pokemonDataService);

        pokemonEvolutionController.pokedexController = pokedexController;
        pokemonEvolutionController.pokemonController = pokemonController;
        pokemonEvolutionController.pokemonDataService = pokemonDataService;
        pokemonEvolutionController.pokemonEvolutionService = pokemonEvolutionService;

        pokemonEvolutionController.pokemonDataList = pokemonDataService.getPokemonDataList();
        pokemonEvolutionController.pokemonEvolutionMap =
                pokemonEvolutionService.getPokemonEvolutionMap();

        return pokemonEvolutionController;
    }

    @Test
    public void testIsNotEvolved() {
        // defaut level is 5
        Pokemon pokemon =
                Pokemon.builder()
                        .pokedexNumber(1)
                        .currentHp(45)
                        .hp(45)
                        .attack(49)
                        .defense(49)
                        .specialAttack(65)
                        .specialDefense(65)
                        .speed(45)
                        .build();

        PokemonEvolutionController PokemonEvolutionController = getPokemonEvolutionController();

        // add pokemon
        PokemonEvolutionController.pokemonController.pokemonRepository.add(pokemon);
        boolean isEvolvedAndUpdated = false;
        // evolve
        isEvolvedAndUpdated = PokemonEvolutionController.evolvePokemon(pokemon.getId().toString());

        assertThat(isEvolvedAndUpdated).isFalse();
    }

    @Test
    public void testIsEvolved() {
        // set level to 10
        Pokemon pokemon =
                Pokemon.builder()
                        .pokedexNumber(1)
                        .currentHp(45)
                        .level(10)
                        .hp(45)
                        .attack(49)
                        .defense(49)
                        .specialAttack(65)
                        .specialDefense(65)
                        .speed(45)
                        .build();

        PokemonEvolutionController PokemonEvolutionController = getPokemonEvolutionController();

        // add pokemon
        PokemonEvolutionController.pokemonController.pokemonRepository.add(pokemon);
        boolean isEvolvedAndUpdated = false;
        // evolve
        isEvolvedAndUpdated = PokemonEvolutionController.evolvePokemon(pokemon.getId().toString());

        String msg = PokemonEvolutionController.buildEvolveMessage(pokemon.getId().toString());
        String expected = "your Bulbasaur evolved into Ivysaur!";

        assertThat(isEvolvedAndUpdated).isTrue();
        assertThat(msg).isEqualTo(expected);
    }
}

package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import edu.northeastern.cs5500.starterbot.service.PokemonEvolutionService;
import org.junit.Test;

public class BattleControllerTest {
    private BattleController getBattleController() {

        /** Mock Injection */
        BattleController battleController = new BattleController();
        // service
        PokemonDataService pokemonDataService =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/pokeDataTest_2.json");
        // pokedex controller
        PokedexController pokedexController = new PokedexController(pokemonDataService);
        // pokemon
        PokemonController pokemonController =
                new PokemonController(new InMemoryRepository<>(), pokemonDataService);

        battleController.pokedexController = pokedexController;
        battleController.pokemonController = pokemonController;
        battleController.trainerController = new TrainerController(new InMemoryRepository<>());

        return battleController;
    }

    @Test
    public void testEvolvedString() {
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
        Trainer trainer =
                Trainer.builder()
                        .discordUserId("testDiscordUserId")
                        .balance(10) // set the initial balance
                        .build();

        BattleController battleController = getBattleController();

        // add pokemon
        battleController.pokemonController.pokemonRepository.add(pokemon);
        // add to trainer inventory
        trainer.getPokemonInventory().add(pokemon.getId());
        // add trainer
        battleController.trainerController.trainerRepository.add(trainer);
        boolean isEvolvedAndUpdated = false;
        // evolve
        PokemonEvolutionService pokemonEvolutionService =
                new PokemonEvolutionService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/evolution-chainTest.json");
        battleController.pokemonController.pokemonEvolutionList =
                pokemonEvolutionService.getPokemonEvolutionList();
        if (pokemon.canEvolve()) {
            isEvolvedAndUpdated =
                    battleController.pokemonController.evolvePokemon(
                            pokemon.getId().toString(), battleController.pokedexController);
        }

        String msg = battleController.buildEvolveMessage(pokemon.getId().toString());
        String expected =
                "Your Bulbasaur evolved into Ivysaur!\nIvysaur was added to your inventory.";

        assertThat(isEvolvedAndUpdated).isTrue();
        assertThat(msg).isEqualTo(expected);
    }
}

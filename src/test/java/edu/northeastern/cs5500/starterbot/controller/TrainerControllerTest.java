package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.exception.InsufficientFoodException;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrainerControllerTest {

    private TrainerController getTrainerController() {
        TrainerController trainerController = new TrainerController(new InMemoryRepository<>());
        return trainerController; // Empty repo
    }

    private Trainer trainer =
            Trainer.builder()
                    .discordUserId("testDiscordUserId")
                    .balance(10) // set the initial balance
                    .build();

    private List<Pokemon> buildPokemonFromDataList(List<PokemonData> datas) {
        List<Pokemon> pokemons = new ArrayList<>();
        for (PokemonData d : datas) {
            Pokemon p =
                    Pokemon.builder()
                            .pokedexNumber(d.getNumber())
                            .currentHp(d.getHp())
                            .hp(d.getHp())
                            .attack(d.getAttack())
                            .defense(d.getDefense())
                            .specialAttack(d.getSpAttack())
                            .specialDefense(d.getSpDefense())
                            .speed(d.getSpeed())
                            .build();
            pokemons.add(p);
        }
        return pokemons;
    }

    @Test
    void testIncreaseTrainerBalance() {

        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        trainerController.increaseTrainerBalance(trainer.getDiscordUserId(), 1);
        assertThat(trainer.getBalance()).isEqualTo(11);
    }

    @Test
    void testDecreaseTrainerBalance() throws InsufficientBalanceException {
        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        trainerController.decreaseTrainerBalance(trainer.getDiscordUserId(), 5);
        assertThat(trainer.getBalance()).isEqualTo(5);
    }

    @Test
    void testDecreaseTrainerBalanceInsufficient() throws InsufficientBalanceException {
        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        assertThrows(
                InsufficientBalanceException.class,
                () -> trainerController.decreaseTrainerBalance(trainer.getDiscordUserId(), 15));
    }

    @Test
    void testAddTrainerFood() {
        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        trainerController.addTrainerFood(trainer.getDiscordUserId(), FoodType.MYSTERYBERRY);
        Map<FoodType, Integer> expectedInventory = new HashMap<>();
        expectedInventory.put(FoodType.MYSTERYBERRY, 1);

        assertEquals(expectedInventory, trainer.getFoodInventory());
    }

    @Test
    void testRemoveTrainerFood() throws InsufficientFoodException {
        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        trainer.getFoodInventory().put(FoodType.MYSTERYBERRY, 1);
        trainerController.removeTrainerFood(trainer.getDiscordUserId(), FoodType.MYSTERYBERRY);
        Map<FoodType, Integer> expectedInventory = new HashMap<>();
        expectedInventory.put(FoodType.MYSTERYBERRY, 0);

        assertEquals(expectedInventory, trainer.getFoodInventory());
    }

    @Test
    void testAddDailyRewardsToTrainer() throws InvalidCheckinDayException {
        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        String discordId = trainer.getDiscordUserId();
        LocalDate today = LocalDate.now();

        // Before adding any daily reward coins
        assertThat(trainerController.getTrainerForMemberId(discordId).getBalance()).isEqualTo(10);

        // Attempt 1: After adding the daily reward coins
        assertThat(trainerController.addDailyRewardsToTrainer(discordId, 10, today)).isEqualTo(20);
        assertThat(trainerController.getTrainerForMemberId(discordId).getBalance()).isEqualTo(20);

        // Attempt 2: Try adding daily reward MORE THAN ONCE in a day
        assertThrows(
                InvalidCheckinDayException.class,
                () ->
                        trainerController.addDailyRewardsToTrainer(
                                trainer.getDiscordUserId(), 30, today));
        assertThat(trainerController.getTrainerForMemberId(discordId).getBalance()).isEqualTo(20);
    }

    @Test
    void testGetTrainerFoodInventory() {
        TrainerController trainerController = getTrainerController();
        trainerController.trainerRepository.add(trainer);
        trainer.getFoodInventory().put(FoodType.GOLDBERRY, 2);

        Map<FoodType, Integer> expectedInventory = new HashMap<>();
        expectedInventory.put(FoodType.MYSTERYBERRY, 0);
        expectedInventory.put(FoodType.BERRY, 0);
        expectedInventory.put(FoodType.GOLDBERRY, 2);

        assertEquals(
                expectedInventory,
                trainerController.getTrainerFoodInventory(trainer.getDiscordUserId()));
    }

    @Test
    void testBuildFoodDetial() {
        TrainerController trainerController = getTrainerController();
        Map<FoodType, Integer> example = new HashMap<>();
        example.put(FoodType.MYSTERYBERRY, 0);
        example.put(FoodType.BERRY, 0);
        example.put(FoodType.GOLDBERRY, 0);
        String expectedOutput =
                "   Gold Berry      🌟 : 0\n"
                        + "   Mystery Berry   🍭 : 0\n"
                        + "   Berry           🫐 : 0\n";

        String actualOutput = trainerController.buildTrainerBerryStockDetail(example);
        // assertThat(actualOutput).isEqualTo(expectedOutput);
    }

    @Test
    void testBuildPokemonInventory10() {
        TrainerController trainerController = getTrainerController();
        PokemonDataService service =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/pokeDataTest_10.json");
        PokedexController pokedexController = new PokedexController(service);
        trainerController.pokedexController = pokedexController;
        List<Pokemon> pokes = buildPokemonFromDataList(service.getPokemonDataList());
        // System.out.println(trainerController.buildPokemonInventoryDetail(pokes));
    }

    @Test
    void testBuildPokemonInventoryGreater10() {
        TrainerController trainerController = getTrainerController();
        PokemonDataService service =
                new PokemonDataService(
                        "src/test/java/edu/northeastern/cs5500/starterbot/resources/pokeDataTest.json");
        PokedexController pokedexController = new PokedexController(service);
        trainerController.pokedexController = pokedexController;
        List<Pokemon> pokes = buildPokemonFromDataList(service.getPokemonDataList());
        // System.out.println(trainerController.buildPokemonInventoryDetail(pokes));
    }
}

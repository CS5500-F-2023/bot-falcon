package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.exception.InsufficientFoodException;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.time.LocalDate;
import java.util.HashMap;
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
        assertThat(trainerController.addDailyRewardsToTrainer(discordId, 10, today)).isEqualTo(20);
        assertThrows(
                InvalidCheckinDayException.class,
                () ->
                        trainerController.addDailyRewardsToTrainer(
                                trainer.getDiscordUserId(), 30, today));
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
        // System.out.println(trainerController.buildTrainerBerryStockDetail(example));
    }
}

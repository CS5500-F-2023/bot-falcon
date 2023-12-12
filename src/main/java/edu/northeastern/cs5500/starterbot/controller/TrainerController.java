package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.exception.InsufficientFoodException;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class TrainerController {

    static final Integer MIN_FOOD_AMOUNT_REQUIRED = 1;

    private static final Integer POKEMON_THRESHOLD = 10;
    private static final Integer POKEMON_PER_ROW_TWO = 2;
    private static final Integer POKEMON_PER_ROW_THREE = 3;

    GenericRepository<Trainer> trainerRepository;

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject
    TrainerController(GenericRepository<Trainer> trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    /**
     * Retrieves the Trainer object associated with the given Discord member ID. If a Trainer with
     * the specified Discord member ID is found, it is returned. Otherwise, a new Trainer object is
     * created with the given Discord member ID and added to the repository.
     *
     * @param discordMemberId The Discord member ID to search for.
     * @return The Trainer object associated with the given Discord member ID.
     */
    @Nonnull
    public Trainer getTrainerForMemberId(String discordMemberId) {
        Collection<Trainer> trainers = trainerRepository.getAll();
        for (Trainer currentTrainer : trainers) {
            if (currentTrainer.getDiscordUserId().equals(discordMemberId)) {
                return currentTrainer;
            }
        }
        Trainer trainer = Trainer.builder().discordUserId(discordMemberId).build();

        return trainerRepository.add(trainer);
    }

    /**
     * Adds a Pokemon to the trainer's inventory.
     *
     * @param discordMemberId The Discord member ID of the trainer.
     * @param pokemonIdString The ID of the Pokemon to be added.
     */
    public void addPokemonToTrainer(String discordMemberId, String pokemonIdString) {
        ObjectId pokemonId = new ObjectId(pokemonIdString);
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        trainer.getPokemonInventory().add(pokemonId);
        trainerRepository.update(trainer);
    }

    /**
     * Increases the balance of a trainer identified by their Discord member ID.
     *
     * @param discordMemberId The Discord member ID of the trainer.
     * @param amount The amount by which to increase the trainer's balance.
     */
    public void increaseTrainerBalance(String discordMemberId, @Nonnegative Integer amount) {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        trainer.setBalance(trainer.getBalance() + amount);
        trainerRepository.update(trainer);
    }

    /**
     * Decreases the balance of a trainer by the specified amount.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @param amount the amount to decrease the balance by
     * @throws InsufficientBalanceException if the trainer's balance is insufficient
     */
    public void decreaseTrainerBalance(String discordMemberId, @Nonnegative Integer amount)
            throws InsufficientBalanceException {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        Integer newBal = trainer.getBalance() - amount;
        if (newBal >= 0) {
            trainer.setBalance(newBal);
        } else {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        trainerRepository.update(trainer);
    }

    /**
     * Adds a food item to the trainer's food inventory. If the food item already exists in the
     * inventory, its quantity is incremented by 1. If the food item does not exist in the
     * inventory, it is added with a quantity of 1.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @param food the food item to be added
     */
    public void addTrainerFood(String discordMemberId, FoodType food) {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        String foodName = food.getUppercaseName();
        Map<String, Integer> foodInventory = trainer.getFoodInventory();
        foodInventory.put(foodName, foodInventory.getOrDefault(foodName, 0) + 1);
        trainerRepository.update(trainer);
    }

    /**
     * Removes a specific food item from the trainer's food inventory.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @param food the type of food to be removed
     * @throws InsufficientFoodException if there is not enough food to remove
     */
    public void removeTrainerFood(String discordMemberId, FoodType food)
            throws InsufficientFoodException {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        Map<String, Integer> foodInventory = trainer.getFoodInventory();
        Integer foodAmount = foodInventory.get(food.getUppercaseName());
        if (foodAmount == null || foodAmount <= 0) {
            throw new InsufficientFoodException("Not enough food to remove");
        }
        foodInventory.put(food.getUppercaseName(), foodAmount - 1);
        trainerRepository.update(trainer);
    }

    /**
     * Retrieves the Pokemon inventory of a trainer identified by their Discord member ID.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @return the list of Pokemon in the trainer's inventory
     */
    public List<Pokemon> getTrainerPokemonInventory(String discordMemberId) {
        List<Pokemon> pokemonInventory = new ArrayList<>();
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        List<ObjectId> pokemonIds = trainer.getPokemonInventory();
        Map<String, ObjectId> indexToObjectIdMap = new HashMap<>(); // map for mongo
        for (int i = 0; i < pokemonIds.size(); i++) {
            ObjectId pokemonId = pokemonIds.get(i);
            String pokeId = pokemonId.toString();
            Pokemon pokemon = pokemonController.getPokemonById(pokeId);
            indexToObjectIdMap.put(Integer.toString(i), pokemonId);
            pokemonInventory.add(pokemon);
        }
        trainer.setIndexToObjectIDMap(indexToObjectIdMap);
        trainerRepository.update(trainer);
        return pokemonInventory;
    }

    /**
     * Builds a detailed inventory of Pokemon string.
     *
     * @param pokemonInventory the list of Pokemon in the inventory
     * @return the formatted string representation of the Pokemon inventory
     */
    public String buildPokemonInventoryDetail(List<Pokemon> pokemonInventory) {
        StringBuilder pokemonInventoryBuilder = new StringBuilder();

        if (pokemonInventory.isEmpty()) {
            pokemonInventoryBuilder.append("Oops....no Pokemon Found.\n\n");
            pokemonInventoryBuilder.append("🐣 Use /spawn to discover and catch new Pokemon!\n");
        } else {
            pokemonInventoryBuilder.append("🎒 Your Pokemon Inventory 🎒\n\n");
            int pokemonPerRow =
                    (pokemonInventory.size() <= POKEMON_THRESHOLD)
                            ? POKEMON_PER_ROW_TWO
                            : POKEMON_PER_ROW_THREE;

            /** find max length pokemon name */
            int maxTextWidth = 0;
            for (int i = 0; i < pokemonInventory.size(); i++) {
                Pokemon pokemon = pokemonInventory.get(i);
                PokemonSpecies species =
                        pokedexController.getPokemonSpeciesByREALPokedex(
                                pokemon.getPokedexNumber());

                String pokemonText = String.format("🔘 %d. %s", i + 1, species.getName());
                maxTextWidth = Math.max(maxTextWidth, pokemonText.length());
            }
            /** build single pokemon name string */
            for (int i = 0; i < pokemonInventory.size(); i++) {
                Pokemon pokemon = pokemonInventory.get(i);
                PokemonSpecies species =
                        pokedexController.getPokemonSpeciesByREALPokedex(
                                pokemon.getPokedexNumber());

                String pokemonText = String.format("🔘 %d. %s", i + 1, species.getName());
                pokemonInventoryBuilder.append(
                        String.format("%-" + maxTextWidth + "s", pokemonText));

                if ((i + 1) % pokemonPerRow == 0 || i == pokemonInventory.size() - 1) {
                    pokemonInventoryBuilder.append("\n");
                }
            }
            pokemonInventoryBuilder.append("\n🔍 Learn more about your Pokemon with /my!");
        }
        return "```" + pokemonInventoryBuilder.toString() + "```";
    }

    /**
     * Retrieves a Pokemon from the trainer's inventory based on the specified index.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @param index the index of the Pokemon in the inventory
     * @return the Pokemon at the specified index
     * @throws InvalidInventoryIndexException if the index is invalid or the inventory is empty
     */
    public Pokemon getPokemonFromInventory(String discordMemberId, Integer index)
            throws InvalidInventoryIndexException {
        List<Pokemon> pokemonInventory = getTrainerPokemonInventory(discordMemberId);
        if (pokemonInventory.isEmpty() || index < 0 || index >= pokemonInventory.size()) {
            throw new InvalidInventoryIndexException("Invalid index");
        } else {
            ObjectId objectId =
                    getTrainerForMemberId(discordMemberId)
                            .getIndexToObjectIDMap()
                            .get(Integer.toString(index));
            return pokemonController.getPokemonById(objectId.toString());
        }
    }

    /**
     * Return the updated balance of the trainer after adding the daily reward coins
     *
     * @param discordMemberId Discord member ID of the specific trainer as String
     * @param amount Amount to be added to the balance of the specific balance as Integer
     * @param curDate Current date as LocalDate
     * @return The update balance as Integer
     * @throws InvalidCheckinDayException if the cur date is not strictly greater than previous
     *     checkin date
     */
    public Integer addDailyRewardsToTrainer(
            String discordMemberId, Integer amount, LocalDate curDate)
            throws InvalidCheckinDayException {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        if (curDate.isAfter(trainer.getLastCheckIn())) {
            trainer.setBalance(trainer.getBalance() + amount);
            trainer.setLastCheckIn(curDate);
            trainerRepository.update(trainer); // now in memory so automatically update
            return trainer.getBalance();
        } else {
            throw new InvalidCheckinDayException(
                    "Current checkin date must be after prev checkin date");
        }
    }

    /**
     * Retrieves the Food inventory of a trainer identified by their Discord member ID.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @return the Map of fodd in the trainer's food inventory
     */
    public Map<String, Integer> getTrainerFoodInventory(String discordMemberId) {
        Map<String, Integer> foodInventory = new HashMap<>();
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);

        Map<String, Integer> food = trainer.getFoodInventory();

        for (FoodType type : FoodType.values()) {
            int count = food.getOrDefault(type.getUppercaseName(), 0);
            foodInventory.put(type.getUppercaseName(), count);
        }
        return foodInventory;
    }
}

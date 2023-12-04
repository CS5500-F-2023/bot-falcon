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
    private static final String BOARD_LINE = "----------------------------\n";
    private static final Integer MIN_BALANCE = 10;

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

    public void addPokemonToTrainer(String discordMemberId, String pokemonIdString) {
        ObjectId pokemonId = new ObjectId(pokemonIdString);
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        trainer.getPokemonInventory().add(pokemonId);
        trainerRepository.update(trainer); // now in memory so automatically update
    }

    public void increaseTrainerBalance(String discordMemberId, @Nonnegative Integer amount) {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        trainer.setBalance(trainer.getBalance() + amount);
        trainerRepository.update(trainer); // now in memory so automatically update
    }

    public void decreaseTrainerBalance(String discordMemberId, @Nonnegative Integer amount)
            throws InsufficientBalanceException {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        Integer newBal = trainer.getBalance() - amount;
        if (newBal >= 0) {
            trainer.setBalance(newBal);
        } else {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        trainerRepository.update(trainer); // now in memory so automatically update
    }

    public void addTrainerFood(String discordMemberId, FoodType food) {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        if (!trainer.getFoodInventory().containsKey(food)) {
            trainer.getFoodInventory().put(food, 1);
        } else {
            trainer.getFoodInventory().put(food, trainer.getFoodInventory().get(food) + 1);
        }
        trainerRepository.update(trainer);
    }

    public void removeTrainerFood(String discordMemberId, FoodType food)
            throws InsufficientFoodException {
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        Integer foodAmount = trainer.getFoodInventory().get(food);
        if (foodAmount == null || foodAmount <= 0) {
            throw new InsufficientFoodException("Not enough food to remove");
        }
        trainer.getFoodInventory().put(food, trainer.getFoodInventory().get(food) - 1);
        trainerRepository.update(trainer);
    }

    /**
     * Builds the trainer statistics for a given Discord member ID.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @return a formatted string containing the trainer statistics
     */
    public String buildTrainerStats(String discordMemberId) {
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        Map<FoodType, Integer> food = getTrainerFoodInventory(discordMemberId);
        String foodDetail = buildTrainerBerryStockDetail(food);
        StringBuilder statsBuilder = new StringBuilder();

        /** build basic stats */
        statsBuilder.append("📊 Your Stats 📊\n");
        statsBuilder.append(BOARD_LINE);
        statsBuilder.append("   Balance         💰 : ").append(trainer.getBalance()).append("\n");
        statsBuilder
                .append("   Pokemon Numbers 🎒 : ")
                .append(trainer.getPokemonInventory().size())
                .append("\n");
        statsBuilder.append("\n🍇 Your Berry Inventory 🍇\n");
        statsBuilder.append(BOARD_LINE);
        statsBuilder.append(foodDetail).append("\n");

        /** customize hint base on stats */
        if (!trainer.getPokemonInventory().isEmpty()) {
            statsBuilder.append("🔍 Explore your Pokemon inventory with /pokemon!\n");
        } else {
            statsBuilder.append("🐣 Use /spawn to discover and catch new Pokemon!\n");
        }
        if (trainer.getBalance() <= MIN_BALANCE) {
            statsBuilder.append(
                    "💵 Boost your balance by claiming your daily rewards with /daily!\n");
        }
        if (food.containsValue(0)) {
            statsBuilder.append("😋 Refill your berry stock at the shop using /shop!");
        }

        return "```" + statsBuilder.toString() + "```";
    }

    /**
     * Builds a string representation of the trainer's berry stock details.
     *
     * @param food a map containing the quantity of each food type
     * @return a string representation of the trainer's berry stock details
     */
    protected String buildTrainerBerryStockDetail(Map<FoodType, Integer> food) {
        StringBuilder foodBuilder = new StringBuilder();
        for (Map.Entry<FoodType, Integer> entry : food.entrySet()) {
            foodBuilder.append(
                    String.format(
                            "   %-15s %s : %d\n",
                            entry.getKey().getName(), entry.getKey().getEmoji(), entry.getValue()));
        }
        return foodBuilder.toString();
    }

    /**
     * Retrieves the Pokemon inventory of a trainer identified by their Discord member ID.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @return the list of Pokemon in the trainer's inventory
     */
    public List<Pokemon> getTrainerPokemonInventory(String discordMemberId) {
        List<Pokemon> pokemonInventory = new ArrayList<>(); // TODO, consider potential duplicates
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        List<ObjectId> pokemonIds = trainer.getPokemonInventory();
        for (ObjectId pokemonId : pokemonIds) {
            String pokeId = pokemonId.toString();
            Pokemon pokemon = pokemonController.getPokemonById(pokeId);
            pokemonInventory.add(pokemon);
        }
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
                        pokedexController.getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());

                String pokemonText = String.format("🔘 %d. %s", i + 1, species.getName());
                maxTextWidth = Math.max(maxTextWidth, pokemonText.length());
            }
            /** build single pokemon name string */
            for (int i = 0; i < pokemonInventory.size(); i++) {
                Pokemon pokemon = pokemonInventory.get(i);
                PokemonSpecies species =
                        pokedexController.getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());

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
        List<Pokemon> pokemonInventory = this.getTrainerPokemonInventory(discordMemberId);
        if (pokemonInventory.isEmpty() || index < 0 || index >= pokemonInventory.size()) {
            throw new InvalidInventoryIndexException("Invalid index");
        } else {
            return pokemonInventory.get(index);
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
     * @return the list of Pokemon in the trainer's inventory
     */
    public Map<FoodType, Integer> getTrainerFoodInventory(String discordMemberId) {
        Map<FoodType, Integer> foodInventory = new HashMap<>();
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);

        Map<FoodType, Integer> food = trainer.getFoodInventory();

        for (FoodType type : FoodType.values()) {
            int count = food.getOrDefault(type, 0);
            foodInventory.put(type, count);
        }
        return foodInventory;
    }
}

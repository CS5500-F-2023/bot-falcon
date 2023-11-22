package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class TrainerController {
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

    /**
     * Builds the trainer statistics for a given Discord member ID.
     *
     * @param discordMemberId the Discord member ID of the trainer
     * @return a formatted string containing the trainer statistics
     */
    public String buildTrainerStats(String discordMemberId) {
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        return String.format(
                "```Balance: %d\nPokemon Numbers: %d\nBerry Stock: 🫐\n```",
                trainer.getBalance(), trainer.getPokemonInventory().size());
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
}

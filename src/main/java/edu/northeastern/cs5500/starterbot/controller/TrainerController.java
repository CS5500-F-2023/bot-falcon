package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.exception.InvalidCheckinDayException;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
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

    /**
     * Return a trainer's status, including pokemon collection, balance
     *
     * @param discordMemberId
     * @return trainer stats
     */
    public Map<String, String> getTrainerStats(String discordMemberId) {
        Map<String, String> trainerStats = new HashMap<>();
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        // get stats
        Integer currBal = trainer.getBalance();
        List<Pokemon> pokemonInventory = this.getTrainerPokemonInventory(discordMemberId);

        StringBuilder trainerStatsBuilder = new StringBuilder();
        for (Pokemon pokemon : pokemonInventory) {
            PokemonSpecies species =
                    pokedexController.getePokemonSpeciesByNumber(pokemon.getPokedexNumber());
            String pokeName = species.getName();
            trainerStatsBuilder.append(pokeName).append(", ");
        }
        String pokeNames = trainerStatsBuilder.toString().replaceAll(", $", "");

        trainerStats.put("Balance", Integer.toString(currBal));
        trainerStats.put("PokemonNumbers", Integer.toString(pokemonInventory.size()));
        trainerStats.put("PokemonInventory", pokeNames);

        return trainerStats;
    }

    public List<Pokemon> getTrainerPokemonInventory(String discordMemberId) {
        List<Pokemon> pokemonInventory = new ArrayList<>();
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        List<ObjectId> pokemonIds = trainer.getPokemonInventory();
        for (ObjectId pokemonId : pokemonIds) {
            String pokeId = pokemonId.toString();
            Pokemon pokemon = pokemonController.getPokemonById(pokeId);
            pokemonInventory.add(pokemon);
        }
        return pokemonInventory;
    }

    public Pokemon getPokemonFromInventory(String discordMemberId, Integer index)
            throws InvalidInventoryIndexException {
        List<Pokemon> pokemonInventory = this.getTrainerPokemonInventory(discordMemberId);
        if (pokemonInventory.isEmpty() || index < 0 || index > pokemonInventory.size()) {
            throw new InvalidInventoryIndexException("Invalid index");
        }
        return pokemonInventory.get(index);
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

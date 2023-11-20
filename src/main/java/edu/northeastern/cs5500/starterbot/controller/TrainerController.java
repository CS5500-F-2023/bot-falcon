package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
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
        Trainer trainer = getTrainerForMemberId(discordMemberId);
        // get stats
        Integer currBal = trainer.getBalance();
        List<ObjectId> pokemonInventory = getTrainerPokemonInventory(discordMemberId);

        StringBuilder sb = new StringBuilder();
        for (ObjectId pokemonId : pokemonInventory) {
            String pokeId = pokemonId.toString();
            Pokemon pokemon = pokemonController.getPokemonById(pokeId);
            PokemonSpecies species =
                    pokedexController.getePokemonSpeciesByNumber(pokemon.getPokedexNumber());
            String pokeName = species.getName();
            sb.append(pokeName).append(", ");
        }
        String pokeNames = sb.toString().replaceAll(", $", "");

        trainerStats.put("Balance", Integer.toString(currBal));
        trainerStats.put("PokemonNumbers", Integer.toString(pokemonInventory.size()));
        trainerStats.put("PokemonInventory", pokeNames);

        return trainerStats;
    }

    private List<ObjectId> getTrainerPokemonInventory(String discordMemberId) {
        Trainer trainer = this.getTrainerForMemberId(discordMemberId);
        return trainer.getPokemonInventory();
    }
}

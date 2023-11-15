package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import java.util.Collection;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class TrainerController {
    GenericRepository<Trainer> trainerRepository;

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

        // trainer.setDiscordUserId(discordMemberId);
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
}

package edu.northeastern.cs5500.starterbot.repository;

import dagger.Module;
import dagger.Provides;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import edu.northeastern.cs5500.starterbot.model.UserPreference;

@Module
public class RepositoryModule {
    // NOTE: You can use the following lines if you'd like to store objects in memory.
    // NOTE: The presence of commented-out code in your project *will* result in a lowered grade.

    /** Pokemon mongodb */
    @Provides
    public GenericRepository<Pokemon> providePokemonRepository(
            MongoDBRepository<Pokemon> repository) {
        return repository;
    }

    @Provides
    public Class<Pokemon> providePokemon() {
        return Pokemon.class;
    }

    /** Trainer mongodb */
    @Provides
    public GenericRepository<Trainer> provideTrainerRepository(
            MongoDBRepository<Trainer> repository) {
        return repository;
    }

    @Provides
    public Class<Trainer> provideTrainer() {
        return Trainer.class;
    }

    @Provides
    public GenericRepository<UserPreference> provideUserPreferencesRepository(
            InMemoryRepository<UserPreference> repository) {
        return repository;
    }
}

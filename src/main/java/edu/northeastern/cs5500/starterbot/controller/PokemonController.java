package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.Pokemon.PokemonBuilder;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class PokemonController {

    static final Integer MYSTERYBERRY_LEVEL_UPDATE = 5;
    static final Integer BERRY_LEVEL_UPDATE = 10;
    static final Integer GOLDBERRY_LEVEL_UPDATE = 30;

    GenericRepository<Pokemon> pokemonRepository;

    @Inject PokemonDataService pokemonDataService;

    @Inject
    PokemonController(GenericRepository<Pokemon> pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    /**
     * Create a new Pokemon of the specified number and add it to the repo
     *
     * @param pokedexNumber the number of the Pokemon to spawn
     * @return a new Pokemon with a unique ID
     */
    @Nonnull
    Pokemon spawnPokemon(int pokedexNumber) {
        PokemonBuilder builder = Pokemon.builder();
        builder.pokedexNumber(pokedexNumber);
        List<PokemonData> pokemonDataList = pokemonDataService.getPokemonDataList();
        for (PokemonData data : pokemonDataList) {
            builder.currentHp(data.getHp());
            builder.hp(data.getHp());
            builder.attack(data.getAttack());
            builder.defense(data.getDefense());
            builder.specialAttack(data.getSpAttack());
            builder.specialDefense(data.getSpDefense());
            builder.speed(data.getSpeed());
        }
        return Objects.requireNonNull(
                pokemonRepository.add(Objects.requireNonNull(builder.build())));
    }

    /**
     * Spawns a random Pokemon.
     *
     * @return The spawned Pokemon.
     */
    public Pokemon spawnRandonPokemon() {
        int[] myNumbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}; // TODO update for actual resource
        int randomIndex = (new Random()).nextInt(myNumbers.length);
        return spawnPokemon(myNumbers[randomIndex]);
    }

    /**
     * Retrieves a Pokemon object by its ID.
     *
     * @param pokemonID the ID of the Pokemon to retrieve
     * @return the Pokemon object with the specified ID, or null if not found
     */
    public Pokemon getPokemonById(String pokemonID) {
        return pokemonRepository.get(new ObjectId(pokemonID));
    }

    /**
     * Builds a string representation of the Pokemon's stats based on its ID.
     *
     * @param pokemonIdString The ID of the Pokemon.
     * @return A string containing the Pokemon's stats.
     */
    public String buildPokemonStats(String pokemonIdString) {
        Pokemon pokemon = getPokemonById(pokemonIdString);

        // Build the formatted string with the Pokemon's stats
        return String.format(
                "Level: %d\nHp: %d\nAttack: %d\nDefense: %d\nSpecial Attack: %d\nSpecial Defense: %d\nSpeed: %d",
                pokemon.getLevel(),
                pokemon.getHp(),
                pokemon.getAttack(),
                pokemon.getDefense(),
                pokemon.getSpecialAttack(),
                pokemon.getSpecialDefense(),
                pokemon.getSpeed());
    }

    public void increasePokemonLevelByFood(Pokemon pokemon, FoodType food) {
        Integer currentLevel = pokemon.getLevel();
        if (food.equals(FoodType.MYSTERYBERRY)) {
            pokemon.setLevel(currentLevel + MYSTERYBERRY_LEVEL_UPDATE);
        }
        if (food.equals(FoodType.BERRY)) {
            pokemon.setLevel(currentLevel + BERRY_LEVEL_UPDATE);
        }
        if (food.equals(FoodType.GOLDBERRY)) {
            pokemon.setLevel(currentLevel + GOLDBERRY_LEVEL_UPDATE);
        }
        pokemonRepository.update(pokemon);
    }
}

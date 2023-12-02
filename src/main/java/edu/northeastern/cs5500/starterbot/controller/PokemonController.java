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

    GenericRepository<Pokemon> pokemonRepository;

    @Inject PokemonDataService pokemonDataService;

    List<PokemonData> pokemonDataList;
    private static final Integer TOTAL_POKEMON = 1292;

    @Inject
    PokemonController(
            GenericRepository<Pokemon> pokemonRepository, PokemonDataService pokemonDataService) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonDataService = pokemonDataService;
    }

    /**
     * Create a new Pokemon of the specified number and add it to the repo.
     *
     * @param listIndex the number of the Pokemon to spawn
     * @return a new Pokemon with a unique ID
     */
    @Nonnull
    Pokemon spawnPokemon(int listIndex) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        PokemonBuilder builder = Pokemon.builder();
        PokemonData data = this.pokemonDataList.get(listIndex);

        builder.pokedexNumber(data.getNumber());
        builder.currentHp(data.getHp());
        builder.hp(data.getHp());
        builder.attack(data.getAttack());
        builder.defense(data.getDefense());
        builder.specialAttack(data.getSpAttack());
        builder.specialDefense(data.getSpDefense());
        builder.speed(data.getSpeed());
        return Objects.requireNonNull(
                pokemonRepository.add(Objects.requireNonNull(builder.build())));
    }

    /**
     * Spawns a random Pokemon.
     *
     * @return The spawned Pokemon
     */
    public Pokemon spawnRandonPokemon() {
        int randomIndex = (new Random()).nextInt(TOTAL_POKEMON);
        return spawnPokemon(randomIndex);
    }

    /**
     * Spawns a NPC Pokemon for battle, matching the trainer's Pokemon level. The method ensures the
     * NPC Pokemon's relative strength is within 0.8 to 1.2 times that of the trainer's Pokemon. It
     * also avoids selecting an NPC Pokemon of the same species as the trainer's. If no ideal match
     * is found within the strength range, the closest match is returned.
     *
     * @param trPokemon The trainer's Pokemon in the battle
     * @return A NPC Pokemon adjusted to a suitable level for the battle
     */
    public Pokemon spawnNpcPokemonForBattle(Pokemon trPokemon) {
        int maxAttempt = 100;
        Pokemon closestNpcPokemon = this.spawnRandonPokemon();
        double closestDistance = 10000.0;
        while (maxAttempt > 0) {
            maxAttempt--;
            Pokemon npcPokemon = this.spawnRandonPokemon();
            // Ideally we want to battle with a different species
            if (trPokemon.getPokedexNumber().equals(npcPokemon.getPokedexNumber())) continue;
            npcPokemon.setLevel(trPokemon.getLevel());
            double relStrength = Pokemon.getRelStrength(trPokemon, npcPokemon);
            if (relStrength < 0.8 || relStrength > 1.2) return npcPokemon;
            if (Math.abs(relStrength - 1.0) < closestDistance) {
                closestDistance = Math.abs(relStrength - 1.0);
                closestNpcPokemon = npcPokemon;
            }
        }
        return closestNpcPokemon;
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
     * @param pokemonIdString The ID of the Pokemon
     * @return A string containing the Pokemon's stats
     */
    public String buildPokemonStats(String pokemonIdString) {
        Pokemon pokemon = getPokemonById(pokemonIdString);

        // Build the formatted string with the Pokemon's stats
        return String.format(
                "Level: %d\nXP: %d\nHp: %d\nAttack: %d\nDefense: %d\nSpecial Attack: %d\nSpecial Defense: %d\nSpeed: %d",
                pokemon.getLevel(),
                pokemon.getExPoints(),
                pokemon.getHp(),
                pokemon.getAttack(),
                pokemon.getDefense(),
                pokemon.getSpecialAttack(),
                pokemon.getSpecialDefense(),
                pokemon.getSpeed());
    }

    /**
     * Increases the experience points of a specified Pokemon and updates its level if necessary.
     *
     * @param pokemonIdStr The unique identifier of the Pokemon as a string
     * @param expGained The amount of experience points to be added to the Pokemon
     * @return true if the Pokemon levels up as a result of the added EX points, otherwise false
     */
    public boolean increasePokemonExp(String pokemonIdStr, Integer expGained) {
        Pokemon pokemon = getPokemonById(pokemonIdStr);
        boolean levelUp = pokemon.setExPoints(pokemon.getExPoints() + expGained);
        pokemonRepository.update(pokemon);
        return levelUp;
    }

    public void increasePokemonExpByFood(String pokemonIdStr, FoodType food) {
        increasePokemonExp(pokemonIdStr, food.getExp());
    }
}

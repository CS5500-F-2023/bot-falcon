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

    private static final double RELATIVE_STRENGTH_BASE = 1.0;
    private static final double RELATIVE_STRENGTH_THRESHOLD = 0.2;

    GenericRepository<Pokemon> pokemonRepository;

    @Inject PokemonDataService pokemonDataService;

    List<PokemonData> pokemonDataList;

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
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        int randomIndex = (new Random()).nextInt(this.pokemonDataList.size());
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
        double closestDistance = 100.0;
        Pokemon closestNpcPokemon = spawnRandonPokemon();

        while (maxAttempt > 0) {
            maxAttempt--;
            Pokemon npcPokemon = spawnRandonPokemon();
            // Ideally we want to battle with a different species
            if (trPokemon.getPokedexNumber().equals(npcPokemon.getPokedexNumber())) continue;

            // TODO (zqy): adjust the NPC Pokemon's level subject to the evolution impl
            int addedExp =
                    (trPokemon.getLevel() - Pokemon.DEFAULT_LEVEL) * Pokemon.LEVEL_UP_THRESHOLD
                            + (trPokemon.getExPoints() - Pokemon.DEFAULT_XP);
            npcPokemon.increaseExpPts(addedExp);

            // Check relative strength
            double relStrength = Pokemon.getRelStrength(trPokemon, npcPokemon);
            double strengthDist = Math.abs(relStrength - RELATIVE_STRENGTH_BASE);
            if (strengthDist < RELATIVE_STRENGTH_THRESHOLD) {
                return npcPokemon;
            }
            if (strengthDist < closestDistance) {
                closestDistance = strengthDist;
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
     *     <p>Sample: Level 🌟 : 5 XP 📊 : 10 Hp ❤️ : 65 Attack ⚔️ : 80 Defense 🛡️ : 140 Special
     *     Attack 🔥 : 40 Special Defense 🛡️ : 70 Speed 🏃‍♂️ : 70
     */
    public String buildPokemonStats(String pokemonIdString) {
        Pokemon pokemon = getPokemonById(pokemonIdString);

        // Build the formatted string with the Pokemon's stats
        StringBuilder pokemonStatsBuilder = new StringBuilder();
        pokemonStatsBuilder.append("Level         : 🌟 ").append(pokemon.getLevel()).append("\n");
        pokemonStatsBuilder
                .append("XP            : 📊 ")
                .append(pokemon.getExPoints())
                .append("\n");
        pokemonStatsBuilder.append("Hp            : ❤️ ").append(pokemon.getHp()).append("\n");
        pokemonStatsBuilder
                .append("Speed         : 🏃‍♂️ ")
                .append(pokemon.getSpeed())
                .append("\n");
        pokemonStatsBuilder.append(
                String.format(
                        "%s        : ⚔️ Phys. %-3d | 🔮 Sp. %-3d\n",
                        "Attack", pokemon.getAttack(), pokemon.getSpecialAttack()));
        pokemonStatsBuilder.append(
                String.format(
                        "%s       : 🛡️ Phys. %-3d | 🛡️ Sp. %-3d\n",
                        "Defense", pokemon.getDefense(), pokemon.getSpecialDefense()));

        return pokemonStatsBuilder.toString();
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
        boolean levelUp = pokemon.increaseExpPts(expGained);
        pokemonRepository.update(pokemon);
        return levelUp;
    }

    public void increasePokemonExpByFood(String pokemonIdStr, FoodType food) {
        increasePokemonExp(pokemonIdStr, food.getExp());
    }
}

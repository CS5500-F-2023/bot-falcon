package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.model.PokemonEvolution;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import edu.northeastern.cs5500.starterbot.service.PokemonEvolutionService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class PokemonController {

    private static final int RELATIVE_STRENGTH_THRESHOLD = 2;

    GenericRepository<Pokemon> pokemonRepository;

    @Inject PokemonDataService pokemonDataService;

    @Inject PokedexController pokedexController;

    @Inject PokemonEvolutionService pokemonEvolutionService;

    List<PokemonData> pokemonDataList;

    Map<String, PokemonEvolution> pokemonEvolutionMap;

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
        PokemonData data = this.pokemonDataList.get(listIndex);
        Pokemon pokemon = buildPokemon(data);
        return Objects.requireNonNull(pokemonRepository.add(Objects.requireNonNull(pokemon)));
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
     * relative strength of each Pokemon to knock down the opponent shall be within 2 rounds. It
     * also avoids selecting an NPC Pokemon of the same species as the trainer's. If no ideal match
     * is found within the strength range, the closest match is returned.
     *
     * @param trPokemon The trainer's Pokemon in the battle
     * @return A NPC Pokemon adjusted to a suitable level for the battle
     */
    public Pokemon spawnNpcPokemonForBattle(Pokemon trPokemon) {
        int maxAttempt = 100;
        int closestDistance = 100000;
        Pokemon closestNpcPokemon = spawnRandonPokemon();

        while (maxAttempt > 0) {
            maxAttempt--;
            Pokemon npcPokemon = spawnRandonPokemon();
            if (trPokemon.getPokedexNumber().equals(npcPokemon.getPokedexNumber())) continue;

            // TODO (zqy): adjust subject to the evolution impl
            int addedExp =
                    (trPokemon.getLevel() - Pokemon.DEFAULT_LEVEL) * Pokemon.LEVEL_UP_THRESHOLD
                            + (trPokemon.getExPoints() - Pokemon.DEFAULT_XP);
            npcPokemon.increaseExpPts(addedExp);

            int absRelStrength = Math.abs(Pokemon.getRelStrength(trPokemon, npcPokemon));
            if (absRelStrength <= RELATIVE_STRENGTH_THRESHOLD) {
                return npcPokemon;
            }
            if (absRelStrength < closestDistance) {
                closestDistance = absRelStrength;
                closestNpcPokemon = npcPokemon;
            }
            deletePokemonFromRepo(npcPokemon.getId().toString());
        }
        this.pokemonRepository.add(closestNpcPokemon);
        return closestNpcPokemon;
    }

    /** Delete a Pokemon from the repository. */
    public void deletePokemonFromRepo(String pokemonID) {
        this.pokemonRepository.delete(new ObjectId(pokemonID));
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

    // todo(yhr): remove this method after current PRs merged and no conflicts
    /**
     * Builds a string representation of the Pokemon's stats based on its ID.
     *
     * @param pokemonIdString The ID of the Pokemon
     * @return A string containing the Pokemon's stats
     */
    private String buildPokemonStats(String pokemonIdString) {
        Pokemon pokemon = getPokemonById(pokemonIdString);

        // Build the formatted string with the Pokemon's stats
        StringBuilder pokemonStatsBuilder = new StringBuilder();
        pokemonStatsBuilder.append("Level   : 🌟 ").append(pokemon.getLevel()).append("\n");
        pokemonStatsBuilder.append("XP      : 📊 ").append(pokemon.getExPoints()).append("\n");
        pokemonStatsBuilder.append("Hp      : 🩷 ").append(pokemon.getHp()).append("\n");
        pokemonStatsBuilder.append("Speed   : 🏃‍♂️ ").append(pokemon.getSpeed()).append("\n");
        pokemonStatsBuilder.append(
                String.format(
                        "%s  : 🗡️ Phys. %-3d | 🔮 Sp. %-3d\n",
                        "Attack", pokemon.getAttack(), pokemon.getSpecialAttack()));
        pokemonStatsBuilder.append(
                String.format(
                        "%s : 🛡️ Phys. %-3d | 🛡️ Sp. %-3d\n",
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

    /**
     * Builds a Pokemon object based on the provided PokemonData.
     *
     * @param data The PokemonData object containing the data for the Pokemon.
     * @return The built Pokemon object.
     */
    protected Pokemon buildPokemon(PokemonData data) {
        return Pokemon.builder()
                .pokedexNumber(data.getNumber())
                .currentHp(data.getHp())
                .hp(data.getHp())
                .attack(data.getAttack())
                .defense(data.getDefense())
                .specialAttack(data.getSpAttack())
                .specialDefense(data.getSpDefense())
                .speed(data.getSpeed())
                .level(buildPokemonLevel(data.getNumber()))
                .build();
    }

    /**
     * Builds the level of a Pokemon based on its Pokedex number.
     *
     * @param pokedex The Pokedex number of the Pokemon.
     * @return The level of the Pokemon.
     */
    private Integer buildPokemonLevel(Integer pokedex) {
        this.pokemonEvolutionMap = pokemonEvolutionService.getPokemonEvolutionMap();
        PokemonSpecies species = pokedexController.getPokemonSpeciesByREALPokedex(pokedex);
        if (pokemonEvolutionMap.containsKey(species.getName())) {
            PokemonEvolution evolution = pokemonEvolutionMap.get(species.getName());
            return Pokemon.DEFAULT_LEVEL * (evolution.getPrev().size() + 1);
        }
        return Pokemon.DEFAULT_LEVEL;
    }
}

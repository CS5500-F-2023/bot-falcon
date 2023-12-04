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

    @Inject PokedexController pokedexController;

    @Inject PokemonEvolutionService pokemonEvolutionService;

    List<PokemonData> pokemonDataList;

    List<PokemonEvolution> pokemonEvolutionList;

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
        StringBuilder pokemonStatsBuilder = new StringBuilder();
        pokemonStatsBuilder.append("Level   : 🌟 ").append(pokemon.getLevel()).append("\n");
        pokemonStatsBuilder.append("XP      : 📊 ").append(pokemon.getExPoints()).append("\n");
        pokemonStatsBuilder.append("Hp      : ❤️ ").append(pokemon.getHp()).append("\n");
        pokemonStatsBuilder.append("Speed   : 🏃‍♂️ ").append(pokemon.getSpeed()).append("\n");
        pokemonStatsBuilder.append(
                String.format(
                        "%s  : ⚔️ Phys. %-3d | 🔮 Sp. %-3d\n",
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
        boolean levelUp = pokemon.setExPoints(pokemon.getExPoints() + expGained);
        pokemonRepository.update(pokemon);
        return levelUp;
    }

    public void increasePokemonExpByFood(String pokemonIdStr, FoodType food) {
        increasePokemonExp(pokemonIdStr, food.getExp());
    }

    /**
     * Evolves a Pokemon based on its ID.
     *
     * @param pokemonIdString the ID of the Pokemon to evolve
     * @return true if the Pokemon was successfully evolved, false otherwise
     */
    public boolean evolvePokemon(String pokemonIdString) {
        Pokemon pokemon = getPokemonById(pokemonIdString);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
        for (PokemonEvolution pe : pokemonEvolutionList) {
            if (pe.getEvolutionFrom().equalsIgnoreCase(species.getName())) {
                return evolvePokemonByName(pe.getEvolutionFrom(), pe.getEvolutionTo(), pokemon);
            }
        }
        return false; // not evolved
    }

    /** for testing purpose */
    protected boolean evolvePokemon(String pokemonIdString, PokedexController pokedexController) {
        Pokemon pokemon = getPokemonById(pokemonIdString);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
        for (PokemonEvolution pe : pokemonEvolutionList) {
            if (pe.getEvolutionFrom().equalsIgnoreCase(species.getName())) {
                return evolvePokemonByName(pe.getEvolutionFrom(), pe.getEvolutionTo(), pokemon);
            }
        }
        return false; // not evolved
    }

    /**
     * Evolves a Pokemon by its name. Update pokemon stats except level
     *
     * @param pokemonName the name of the Pokemon to evolve
     * @return true if the Pokemon was successfully evolved, false otherwise
     */
    private boolean evolvePokemonByName(
            String evolutionFrom, String evolutionTo, Pokemon currPokemon) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        for (PokemonData data : pokemonDataList) {
            if (data.getSpeciesNames().get("en").equals(evolutionTo)) {
                currPokemon.setPokedexNumber(data.getNumber());
                currPokemon.setCurrentHp(data.getHp());
                currPokemon.setHp(data.getHp());
                currPokemon.setAttack(data.getAttack());
                currPokemon.setDefense(data.getDefense());
                currPokemon.setSpecialAttack(data.getSpAttack());
                currPokemon.setSpecialDefense(data.getSpDefense());
                currPokemon.setSpeed(data.getSpeed());
                currPokemon.setEvolvedFrom(evolutionFrom);
                currPokemon.setEvolved(true);
                Objects.requireNonNull(pokemonRepository.update(currPokemon));
                return true;
            }
        }
        return false; // not evolved
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
                .build();
    }
}

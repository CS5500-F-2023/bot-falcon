package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonData;
import edu.northeastern.cs5500.starterbot.model.PokemonEvolution;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import edu.northeastern.cs5500.starterbot.service.PokemonEvolutionService;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

public class PokemonEvolutionController {

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject PokemonEvolutionService pokemonEvolutionService;

    @Inject PokemonDataService pokemonDataService;

    List<PokemonEvolution> pokemonEvolutionList;

    List<PokemonData> pokemonDataList;

    @Inject
    PokemonEvolutionController() {
        // for dagger
    }

    /**
     * Evolves a Pokemon based on its pokemon ID.
     *
     * @param pokemonIdString the ID of the Pokemon to evolve
     * @return true if the Pokemon was successfully evolved, false otherwise
     */
    public boolean evolvePokemon(String pokemonIdString) {
        this.pokemonEvolutionList = this.pokemonEvolutionService.getPokemonEvolutionList();
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdString);
        if (pokemon.canEvolve()) {
            PokemonSpecies species =
                    pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
            for (PokemonEvolution pe : pokemonEvolutionList) {
                if (pe.getEvolutionFrom().equalsIgnoreCase(species.getName())) {
                    return evolvePokemonByName(pe.getEvolutionFrom(), pe.getEvolutionTo(), pokemon);
                }
            }
        }
        return false; // not evolved
    }

    /**
     * Helper function evolves a Pokemon by its species name. Update from pokemon data: pokedex
     * number, hp, attack, defense, spAttack, spDefense, speed Update from pokemon's previous
     * species: evolvedFrom, isEvolved
     *
     * @param evolutionFrom the name of the Pokemon evolve from
     * @param evolutionTo the name of the Pokemon evolve to
     * @param pokemon the pokemon to be evolved
     * @return true if the Pokemon was successfully evolved, false otherwise
     */
    private boolean evolvePokemonByName(String evolutionFrom, String evolutionTo, Pokemon pokemon) {
        this.pokemonDataList = this.pokemonDataService.getPokemonDataList();
        for (PokemonData data : pokemonDataList) {
            if (data.getSpeciesNames().get("en").equals(evolutionTo)) {
                pokemon.setPokedexNumber(data.getNumber());
                pokemon.setCurrentHp(data.getHp());
                pokemon.setHp(data.getHp());
                pokemon.setAttack(data.getAttack());
                pokemon.setDefense(data.getDefense());
                pokemon.setSpecialAttack(data.getSpAttack());
                pokemon.setSpecialDefense(data.getSpDefense());
                pokemon.setSpeed(data.getSpeed());
                pokemon.setEvolvedFrom(evolutionFrom);
                pokemon.setEvolved(true);

                // update pokemon in the repository
                Objects.requireNonNull(pokemonController.pokemonRepository.update(pokemon));
                return true;
            }
        }
        return false; // not evolved
    }

    /**
     * Builds an evolution message for a given Pokemon ID.
     *
     * @param pokemonIdStr the ID of the Pokemon
     * @return the evolution message
     */
    public String buildEvolveMessage(String pokemonIdStr) {
        // when this method is called the pokemon should already be updated
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());

        return String.format(
                "your %s evolved into %s!",
                pokemon.getEvolvedFrom(), species.getName(), species.getName());
    }

    /**
     * Builds a message containing the evolution stats of a Pokemon.
     *
     * @param pokemonIdStr the ID of the Pokemon
     * @return the message containing the evolution stats
     */
    public String buildEvolveStatsMessage(String pokemonIdStr) {
        Pokemon pokemon = pokemonController.getPokemonById(pokemonIdStr);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
        // todo sapcing to be adjusted base on actual msg
        return String.format("EVOLVE to   💯 :  %s", species.getName());
    }
}
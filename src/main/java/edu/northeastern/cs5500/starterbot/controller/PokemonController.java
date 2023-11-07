package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.Pokemon.PokemonBuilder;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PokemonController {

    GenericRepository<Pokemon> pokemonRepository;

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
        switch (pokedexNumber) {
            case 1: // Bulbasaur
                builder.currentHp(19);
                builder.hp(19);
                builder.attack(9);
                builder.defense(9);
                builder.specialAttack(11);
                builder.specialDefense(11);
                builder.speed(9);
                break;
            case 4: // Charmander
                builder.currentHp(18);
                builder.hp(18);
                builder.attack(18);
                builder.defense(10);
                builder.specialAttack(9);
                builder.specialDefense(11);
                builder.speed(11);
                break;
            case 7: // Squirtle
                builder.currentHp(19);
                builder.hp(19);
                builder.attack(9);
                builder.defense(11);
                builder.specialAttack(10);
                builder.specialDefense(11);
                builder.speed(9);
                break;
            case 19: // Rattata
                builder.currentHp(18);
                builder.hp(18);
                builder.attack(10);
                builder.defense(8);
                builder.specialAttack(7);
                builder.specialDefense(8);
                builder.speed(12);
                break;
            default:
                throw new IllegalStateException();
        }
        return Objects.requireNonNull(
                pokemonRepository.add(Objects.requireNonNull(builder.build())));
    }

    public Pokemon spawnRandonPokemon() {
        int[] myNumbers = {1, 4, 7, 19};
        int randomIndex = (new Random()).nextInt(myNumbers.length);
        return spawnPokemon(myNumbers[randomIndex]);
    }
}

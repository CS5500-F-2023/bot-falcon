package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.service.PokemonDataService;
import org.junit.Test;

public class PokedexControllerTest {

    private PokedexController getPokedexController() {
        PokemonDataService pokemonDataService = new PokemonDataService();
        PokedexController pokedexController = new PokedexController(pokemonDataService);
        return pokedexController;
    }

    @Test
    public void testGetPokemonSpeciesByPokedex() {
        PokedexController pokedexController = getPokedexController();
        PokemonSpecies bulbasaur = pokedexController.getPokemonSpeciesByREALPokedex(1);
        assertThat(bulbasaur.getPokedexNumber()).isEqualTo(1);
    }

    @Test
    public void testBuildSpeciesDetails() {
        PokedexController pokedexController = getPokedexController();
        String bulbasaurDetail = pokedexController.buildSpeciesDetails(0);
        String typeString = "🌿 Grass, ☠️ Poison";
        String expected =
                String.format("Species: %s\nTypes: %s\nPokedex: %d\n", "Bulbasaur", typeString, 1);
        assertThat(bulbasaurDetail).isEqualTo(expected);
    }

    @Test
    public void testSpeciesTypesWithEmoji() {
        PokedexController pokedexController = getPokedexController();
        PokemonSpecies bulbasaur = pokedexController.getPokemonSpeciesByREALPokedex(1);
        String[] expected = {"🌿 Grass", "☠️ Poison"};
        assertThat(bulbasaur.getSpeciesTypes()).isEqualTo(expected);
    }
}

package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.NPCBattle;
import edu.northeastern.cs5500.starterbot.model.NPCBattle.NPCBattleBuilder;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import edu.northeastern.cs5500.starterbot.model.Trainer;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BattleController {

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    BattleController() {
        // No args as we are not saving anythin
        // empty and defined for Dragger
    }

    public NPCBattle setUpNewBattle(String trDiscordMemberId, String trPokemonIdStr) {
        // Get Pokémons
        Pokemon trPokemon = pokemonController.getPokemonById(trPokemonIdStr);
        Pokemon npcPokemon = pokemonController.spawnNpcPokemonForBattle(trPokemon);

        // Get necessary information to set up a battle
        int trPokedex = trPokemon.getPokedexNumber();
        int npcPokedex = npcPokemon.getPokedexNumber();
        Trainer trainer = trainerController.getTrainerForMemberId(trDiscordMemberId);
        PokemonSpecies trSpecies = pokedexController.getPokemonSpeciesByPokedex(trPokedex);
        PokemonSpecies npcSpecies = pokedexController.getPokemonSpeciesByPokedex(npcPokedex);

        // Set up a battle
        NPCBattleBuilder builder = NPCBattle.builder();
        builder.trDiscordId(trDiscordMemberId);
        builder.trPokemonIdStr(trPokemonIdStr);
        builder.trainer(trainer);
        builder.trPokemon(trPokemon);
        builder.trPokeSpecies(trSpecies);
        builder.npcPokemon(npcPokemon);
        builder.npcPokeSpecies(npcSpecies);
        return builder.build();
    }

    public void runBattle(NPCBattle battle) {
        battle.runBattle();
        // the pokemon's level should be updated in runBattle()
        Pokemon trPokemon = battle.getTrPokemon();

        // check if pokemon's level meet evolution criteria and is evolved
        if (trPokemon.canEvolve()
                && pokemonController.evolvePokemon(trPokemon.getId().toString())) {
            trainerController.trainerRepository.update(battle.getTrainer());
        } else {
            // Update trianer and pokemon in the database
            trainerController.trainerRepository.update(battle.getTrainer());
            pokemonController.pokemonRepository.update(battle.getTrPokemon());
        }
    }

    // TODO test if evolution work first, then decide where to insert this msg with @z-q-ying
    // maybe need to add somewhere in NPCBattle class, but not sure how to avoid break the current
    // string build methods
    public String buildEvolveMessage(String trPokemonIdStr) {
        // when this method is called the pokemon should already be updated
        Pokemon trPokemon = pokemonController.getPokemonById(trPokemonIdStr);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(trPokemon.getPokedexNumber());

        return String.format(
                "Your %s evolved into %s!\n%s was added to your inventory.",
                trPokemon.getEvolvedFrom(), species.getName(), species.getName());
    }
}

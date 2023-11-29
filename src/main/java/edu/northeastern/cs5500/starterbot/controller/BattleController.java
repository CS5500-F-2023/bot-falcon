package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.BattleRecord;
import edu.northeastern.cs5500.starterbot.model.NPCBattle;
import edu.northeastern.cs5500.starterbot.model.NPCBattle.NPCBattleBuilder;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
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

    public NPCBattle setUpNewBattle(Pokemon trPokemon) {
        // Get the NPC Pokémon
        Pokemon npcPokemon = pokemonController.spawnNpcPokemonForBattle(trPokemon);

        // Get necessary information to set up a battle
        PokemonSpecies trPokeSpecies =
                pokedexController.getPokemonSpeciesByPokedex(trPokemon.getPokedexNumber());
        PokemonSpecies npcPokeSpecies =
                pokedexController.getPokemonSpeciesByPokedex(npcPokemon.getPokedexNumber());

        // Set up a battle
        NPCBattleBuilder builder = NPCBattle.builder();
        builder.trPokemon(trPokemon);
        builder.trPokeSpecies(trPokeSpecies);
        builder.npcPokemon(npcPokemon);
        builder.npcPokeSpecies(npcPokeSpecies);
        return builder.build();
    }

    public BattleRecord runBattle(String trDiscordMemberId, NPCBattle battle) {
        battle.startBattle();
        BattleRecord battleRecord = battle.getBattleRecord();

        // Update trianer's coins
        Integer coinsGained = battleRecord.getCoinsGained();
        trainerController.increaseTrainerBalance(trDiscordMemberId, coinsGained);

        // Update trainer Pokémon's xp and level up info if applicable
        Integer expGained = battleRecord.getExpGained();
        pokemonController.increasePokemonExp(battle.getTrPokemon().getId().toString(), expGained);
        return battleRecord;
    }
}
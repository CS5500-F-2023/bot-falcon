package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.BattleRecord;
import edu.northeastern.cs5500.starterbot.model.NPCBattle;
import edu.northeastern.cs5500.starterbot.model.NPCBattle.NPCBattleBuilder;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BattleController {

    @Inject PokemonController pokemonController;

    @Inject TrainerController trainerController;

    @Inject
    BattleController() {
        // No args as we are not saving anythin
        // empty and defined for Dragger
    }

    public NPCBattle setUpNewBattle(Pokemon trPokemon) {
        Pokemon npcPokemon = pokemonController.spawnRandonPokemon();
        NPCBattleBuilder builder = NPCBattle.builder();
        builder.trPokemon(trPokemon);
        builder.npcPokemon(npcPokemon);
        return builder.build();
    }

    public BattleRecord runBattle(String trDiscordMemberId, NPCBattle battle) {
        System.out.println("!!! start battle");
        battle.startBattle();
        System.out.println("!!! finish battle");
        BattleRecord record = battle.getBattleRecord();

        // Update trianer's coins
        Integer coinsGained = record.getCoinsGained();
        trainerController.increaseTrainerBalance(trDiscordMemberId, coinsGained);

        // Update trainer pokemon's exp
        Integer expGained = record.getExpGained();
        pokemonController.increasePokemonExp(battle.getTrPokemon().getId().toString(), expGained);

        return record;
    }
}

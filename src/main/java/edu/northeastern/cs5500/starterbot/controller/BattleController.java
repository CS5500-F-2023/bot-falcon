package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.BotConstants;
import edu.northeastern.cs5500.starterbot.model.ColoredMessage;
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

    @Inject PokemonEvolutionController pokemonEvolutionController;

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
        PokemonSpecies trSpecies = pokedexController.getPokemonSpeciesByREALPokedex(trPokedex);
        PokemonSpecies npcSpecies = pokedexController.getPokemonSpeciesByREALPokedex(npcPokedex);

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

        // Update trianer and pokemon in the database
        trainerController.trainerRepository.update(battle.getTrainer());
        pokemonController.pokemonRepository.update(battle.getTrPokemon());

        // Check evolution
        String pokemonId = battle.getTrPokemonIdStr();
        boolean evolved = pokemonEvolutionController.evolvePokemon(pokemonId);
        if (evolved) {
            String s =
                    "```"
                            + pokemonEvolutionController.buildEvolveMessage(pokemonId)
                            + "\n\n"
                            + pokemonEvolutionController.buildEvolveStatsMessage(pokemonId)
                            + "```";
            battle.getMessages().add(new ColoredMessage(s, BotConstants.COLOR_GENERIC));
        }
    }
}

package edu.northeastern.cs5500.starterbot.model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnegative;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BattleRecord {

    private static final Integer BASE_COINS_FOR_WINNER = 20;
    private static final Integer FLOOR_COINS_FOR_WINNER = 7;
    private static final Integer CAP_COINS_FOR_WINNER = 60;

    private static final Integer BASE_EXP_FOR_WINNER = 40;
    private static final Integer FLOOR_EXP_FOR_WINNER = 5;
    private static final Integer CAP_EXP_FOR_WINNER = 80;

    private static final Integer BASE_EXP_FOR_LOSER = 10;
    private static final Integer FLOOR_EXP_FOR_LOSER = 5;
    private static final Integer CAP_EXP_FOR_LOSER = 20;

    @Builder.Default boolean trainerWins = false;
    @Builder.Default boolean canLevelUp = false;
    @Builder.Default @Nonnegative Integer coinsGained = 0; // for trainer
    @Builder.Default @Nonnegative Integer expGained = 0; // for pokemon

    @Builder.Default List<String> battleRounds = new ArrayList<>();

    /**
     * Document each round's info as a string.
     *
     * @param msg each round's info as a string
     */
    public void addBattleRoundInfo(String msg) {
        this.battleRounds.add(msg);
    }

    /**
     * Update the battle stats (coins gained, XP gained, etc.) after the battle ends.
     *
     * @param trPokemon The trainer's Pokemon
     * @param npcPokemon The NPC Pokemon
     * @param trainerWins Whether the trainer wins in the battle
     */
    public void updateFinalResult(Pokemon trPokemon, Pokemon npcPokemon, boolean trainerWins) {
        // Update winner
        this.trainerWins = trainerWins;

        // Get rel strength
        double relativeStrength = Pokemon.getRelStrength(trPokemon, npcPokemon);

        // Update coin and experience
        this.updateCoins(trainerWins, relativeStrength);
        this.updateExperience(trainerWins, relativeStrength);
        this.updateCanLevelUp(trPokemon, expGained);
    }

    public boolean getCanLevelUp() {
        return this.canLevelUp;
    }

    private void updateCoins(boolean trainerWins, double relStrength) {
        if (trainerWins) { // Otherwise, no coins
            coinsGained = (int) (BASE_COINS_FOR_WINNER / relStrength);
            if (coinsGained < FLOOR_COINS_FOR_WINNER) coinsGained = FLOOR_COINS_FOR_WINNER;
            if (coinsGained > CAP_COINS_FOR_WINNER) coinsGained = CAP_COINS_FOR_WINNER;
        }
    }

    private void updateExperience(boolean trainerWins, double relStrength) {
        if (trainerWins) {
            expGained = (int) (BASE_EXP_FOR_WINNER / relStrength);
            if (expGained < FLOOR_EXP_FOR_WINNER) expGained = FLOOR_EXP_FOR_WINNER;
            if (expGained > CAP_EXP_FOR_WINNER) expGained = CAP_EXP_FOR_WINNER;
        } else {
            expGained = (int) (BASE_EXP_FOR_LOSER / relStrength);
            if (expGained < FLOOR_EXP_FOR_LOSER) expGained = FLOOR_EXP_FOR_LOSER;
            if (expGained > CAP_EXP_FOR_LOSER) expGained = CAP_EXP_FOR_LOSER;
        }
    }

    private void updateCanLevelUp(Pokemon trPokemon, Integer xpGained) {
        this.canLevelUp = trPokemon.canLevelUpWithAddedXP(xpGained);
    }
}

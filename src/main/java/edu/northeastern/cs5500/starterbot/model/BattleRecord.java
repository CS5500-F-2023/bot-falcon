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
    @Builder.Default @Nonnegative Integer coinsGained = 0; // for trainer
    @Builder.Default @Nonnegative Integer expGained = 0; // for pokemon

    @Builder.Default List<String> battleRounds = new ArrayList<>();

    public void addBattleRoundInfo(String msg) {
        this.battleRounds.add(msg);
    }

    public void updateFinalResult(Pokemon trPokemon, Pokemon npcPokemon, boolean trainerWins) {
        // Update winner
        this.trainerWins = trainerWins;

        // Get rel strength
        double relativeStrength = Pokemon.getRelStrength(trPokemon, npcPokemon);

        // Update coin and experience
        this.updateCoins(trainerWins, relativeStrength);
        this.updateExperience(trainerWins, relativeStrength);
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
}

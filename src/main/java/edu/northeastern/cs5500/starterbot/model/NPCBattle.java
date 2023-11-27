package edu.northeastern.cs5500.starterbot.model;

import java.util.Random;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NPCBattle {

    Pokemon trPokemon;
    Pokemon npcPokemon;
    @Builder.Default boolean gameOver = Boolean.FALSE;
    @Builder.Default BattleRecord battleRecord = BattleRecord.builder().build();

    public void startBattle() {
        Random random = new Random();
        Pokemon curMovePokemon = random.nextBoolean() ? trPokemon : npcPokemon;

        while (!gameOver) {
            // TODO: zqy: this is just to get it running
            Pokemon defensePokemon = (curMovePokemon.equals(trPokemon) ? npcPokemon : trPokemon);
            Integer newHP = defensePokemon.getCurrentHp() - 10;
            defensePokemon.setCurrentHp(newHP < 0 ? 0 : newHP);

            // TODO: zqy: Update round message

            // Check if the game ends, if not, set curMovePokemon to the opponent's pokemon
            if (trPokemon.getCurrentHp() <= 0) {
                battleRecord.setTrainerWins(false);
                gameOver = true;
            } else if (npcPokemon.getCurrentHp() <= 0) {
                battleRecord.setTrainerWins(true);
                gameOver = true;
            } else {
                curMovePokemon = (curMovePokemon.equals(trPokemon) ? npcPokemon : trPokemon);
            }
        }
    }
}

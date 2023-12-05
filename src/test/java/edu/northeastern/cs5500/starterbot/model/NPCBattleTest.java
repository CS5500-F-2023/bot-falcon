package edu.northeastern.cs5500.starterbot.model;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.northeastern.cs5500.starterbot.exception.InvalidBattleStatusException;
import org.junit.jupiter.api.Test;

// import static com.google.common.truth.Truth.assertThat;

public class NPCBattleTest {

    private Pokemon bulbasaur =
            Pokemon.builder()
                    .pokedexNumber(1)
                    .currentHp(45)
                    .hp(45)
                    .attack(49)
                    .defense(49)
                    .specialAttack(65)
                    .specialDefense(65)
                    .speed(45)
                    .build();

    private Pokemon charmander =
            Pokemon.builder()
                    .pokedexNumber(4)
                    .currentHp(39)
                    .hp(39)
                    .attack(52)
                    .defense(43)
                    .specialAttack(60)
                    .specialDefense(50)
                    .speed(65)
                    .build();

    private Trainer trainer = Trainer.builder().discordUserId("123").balance(20).build();

    private NPCBattle npcBattle =
            NPCBattle.builder()
                    .trDiscordId(trainer.getDiscordUserId())
                    .trPokemonIdStr(bulbasaur.getId().toString())
                    .trainer(trainer)
                    .trPokemon(bulbasaur)
                    .trPokeSpecies(null)
                    .npcPokemon(charmander)
                    .npcPokeSpecies(null)
                    .build();

    @Test
    void testSetCoinsEarnedException() {
        assertThrows(InvalidBattleStatusException.class, () -> npcBattle.setCoinsEarned());
    }

    @Test
    void testSetCoinsEarnedTrainerWins() throws InvalidBattleStatusException {
        npcBattle.setGameOver(true);
        npcBattle.setTrainerWins(true);
        int relStrength = Pokemon.getRelStrength(bulbasaur, charmander);
        assertThat(relStrength).isEqualTo(3);
        int coinsEarned = 20 - relStrength * 2;
        assertThat(coinsEarned).isEqualTo(14);
        npcBattle.setCoinsEarned();
        assertThat(npcBattle.getCoinsEarned()).isEqualTo(14);
    }

    @Test
    void testSetCoinsEarnedTrainerLoses() throws InvalidBattleStatusException {
        npcBattle.setGameOver(true);
        npcBattle.setTrainerWins(false);
        npcBattle.setCoinsEarned();
        assertThat(npcBattle.getCoinsEarned()).isEqualTo(0);
    }

    @Test
    void testSetXpGainedException() {
        assertThrows(InvalidBattleStatusException.class, () -> npcBattle.setXpGained());
    }

    @Test
    void testSetXpGainedTrainerWins() throws InvalidBattleStatusException {
        npcBattle.setGameOver(true);
        npcBattle.setTrainerWins(true);
        int relStrength = Pokemon.getRelStrength(bulbasaur, charmander);
        assertThat(relStrength).isEqualTo(3);
        int xpGained = 40 - relStrength * 2;
        assertThat(xpGained).isEqualTo(34);
        npcBattle.setXpGained();
        assertThat(npcBattle.getXpGained()).isEqualTo(34);
    }

    @Test
    void testSetXpGainedTrainerLoses() throws InvalidBattleStatusException {
        npcBattle.setGameOver(true);
        npcBattle.setTrainerWins(false);
        int relStrength = 3;
        int xpGained = 15 - relStrength * 2;
        assertThat(xpGained).isEqualTo(9);
        npcBattle.setXpGained();
        assertThat(npcBattle.getXpGained()).isEqualTo(9);
    }
}

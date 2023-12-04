package edu.northeastern.cs5500.starterbot.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class PokemonTest {

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

    private Pokemon crobat =
            Pokemon.builder()
                    .pokedexNumber(169)
                    .currentHp(10)
                    .hp(85)
                    .attack(90)
                    .defense(80)
                    .specialAttack(70)
                    .specialDefense(80)
                    .speed(130)
                    .build();

    @Test
    void increaseExpPts() {
        // No level up
        assertThat(bulbasaur.getExPoints()).isEqualTo(10);
        assertThat(bulbasaur.increaseExpPts(89)).isFalse();

        // At the border of leveling up

        // Leveling up twice
    }

    @Test
    void testGenerateHealthBar() {
        String str = "█".repeat(15);
        assertThat(bulbasaur.generateHealthBar()).isEqualTo(str);
        bulbasaur.setCurrentHp(13);
        str = "█".repeat(5) + "░".repeat(10);
        assertThat(bulbasaur.generateHealthBar()).isEqualTo(str);
        bulbasaur.setCurrentHp(1);
        str = "█".repeat(1) + "░".repeat(14);
        assertThat(bulbasaur.generateHealthBar()).isEqualTo(str);
        bulbasaur.setCurrentHp(0);
        str = "░".repeat(15);
        assertThat(bulbasaur.generateHealthBar()).isEqualTo(str);
    }

    @Test
    void getBaseDamage() {
        bulbasaur.setLevel(7);
        charmander.setLevel(6);
    }
}

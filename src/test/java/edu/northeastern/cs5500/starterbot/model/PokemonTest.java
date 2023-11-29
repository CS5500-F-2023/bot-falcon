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

    @Test
    void testSetExPoints1() { // No level up
        assertThat(bulbasaur.setExPoints(99)).isFalse();
        assertThat(bulbasaur.getLevel()).isEqualTo(5);
        assertThat(bulbasaur.getExPoints()).isEqualTo(99);
    }

    @Test
    void testSetExPoints2() { // Border case
        assertThat(bulbasaur.setExPoints(100)).isTrue();
        assertThat(bulbasaur.getLevel()).isEqualTo(6);
        assertThat(bulbasaur.getExPoints()).isEqualTo(0);
    }

    @Test
    void testSetExPoints3() { // Up 2 levels
        assertThat(bulbasaur.getExPoints()).isEqualTo(10);
        assertThat(bulbasaur.setExPoints(240)).isTrue();
        assertThat(bulbasaur.getLevel()).isEqualTo(7);
        assertThat(bulbasaur.getExPoints()).isEqualTo(40);
    }

    @Test
    void testCanLevelUpWithAddedXP() {
        assertThat(bulbasaur.canLevelUpWithAddedXP(89)).isFalse();
        assertThat(bulbasaur.canLevelUpWithAddedXP(90)).isTrue();
        assertThat(bulbasaur.canLevelUpWithAddedXP(91)).isTrue();
        assertThat(bulbasaur.canLevelUpWithAddedXP(190)).isTrue();
    }

    @Test
    void testGetRelStrength1() { // Same species
        assertThat(Pokemon.getRelStrength(bulbasaur, bulbasaur)).isEqualTo(1.0);
    }

    @Test
    void testGetRelStrength2() { // Different species
        double bulStrength = 529.5;
        double charStrength = 523.0;
        double relativeStr1 = Math.round(100.0 * bulStrength / charStrength) / 100.0;
        double relativeStr2 = Math.round(100.0 * charStrength / bulStrength) / 100.0;
        assertThat(Pokemon.getRelStrength(bulbasaur, charmander)).isEqualTo(relativeStr1);
        assertThat(Pokemon.getRelStrength(charmander, bulbasaur)).isEqualTo(relativeStr2);
    }
}

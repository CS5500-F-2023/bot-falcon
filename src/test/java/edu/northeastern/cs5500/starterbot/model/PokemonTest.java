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
    void testIncreaseExpPts() {
        // Case 1: No level up
        assertThat(bulbasaur.increaseExpPts(0)).isFalse();
        assertThat(bulbasaur.getExPoints()).isEqualTo(10); // Default value
        assertThat(bulbasaur.increaseExpPts(89)).isFalse();
        assertThat(bulbasaur.getLevel()).isEqualTo(5);
        assertThat(bulbasaur.getExPoints()).isEqualTo(99); // Threshold is 100

        // // Case 2: Just level up
        assertThat(bulbasaur.increaseExpPts(1)).isTrue();
        assertThat(bulbasaur.getLevel()).isEqualTo(6);
        assertThat(bulbasaur.getExPoints()).isEqualTo(0);

        assertThat(bulbasaur.getHp()).isEqualTo(45 + 3);
        assertThat(bulbasaur.getAttack()).isEqualTo(49 + 3);
        assertThat(bulbasaur.getSpecialAttack()).isEqualTo(65 + 3);
        assertThat(bulbasaur.getSpecialDefense()).isEqualTo(65 + 3);
        assertThat(bulbasaur.getSpeed()).isEqualTo(45 + 3);

        // // Case 3: Leveling up twice
        assertThat(bulbasaur.increaseExpPts(201)).isTrue();
        assertThat(bulbasaur.getLevel()).isEqualTo(8);
        assertThat(bulbasaur.getExPoints()).isEqualTo(1);

        assertThat(bulbasaur.getHp()).isEqualTo(45 + 3 + 6);
        assertThat(bulbasaur.getAttack()).isEqualTo(49 + 3 + 6);
        assertThat(bulbasaur.getSpecialAttack()).isEqualTo(65 + 3 + 6);
        assertThat(bulbasaur.getSpecialDefense()).isEqualTo(65 + 3 + 6);
        assertThat(bulbasaur.getSpeed()).isEqualTo(45 + 3 + 6);
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
    void testGetBaseDamage() {
        // bulbasaur attacks charmander, physical
        int damage = (int) (49.0 * 1.1 - 43.0 * 0.9);
        assertThat(Pokemon.getBaseDamage(bulbasaur, charmander, true)).isEqualTo(damage);

        // bulbasaur attacks charmander, special
        damage = (int) (65.0 * 1.1 - 50.0 * 0.9);
        assertThat(Pokemon.getBaseDamage(bulbasaur, charmander, false)).isEqualTo(damage);

        // bulbasaur attacks crobat, special, floor damage
        assertThat(Pokemon.getBaseDamage(bulbasaur, crobat, false)).isEqualTo(7);
    }

    @Test
    void testGetRelStrength() {
        int pDamage = Pokemon.getBaseDamage(bulbasaur, charmander, true);
        assertThat(pDamage).isEqualTo(15);
        int sDamage = Pokemon.getBaseDamage(bulbasaur, charmander, false);
        assertThat(sDamage).isEqualTo(26);
        int round1 = (int) Math.ceil(charmander.getHp() / ((pDamage + sDamage) / 2.0));
        assertThat(round1).isEqualTo(2); // Round up 1.9

        int pDamage2 = Pokemon.getBaseDamage(charmander, bulbasaur, true);
        assertThat(pDamage2).isEqualTo(13);
        int sDamage2 = Pokemon.getBaseDamage(charmander, bulbasaur, false);
        assertThat(sDamage2).isEqualTo(7);
        int round2 = (int) Math.ceil(bulbasaur.getHp() / ((pDamage2 + sDamage2) / 2.0));
        assertThat(round2).isEqualTo(5); // Round up 4.5

        int relStrength = Pokemon.getRelStrength(bulbasaur, charmander);
        assertThat(relStrength).isEqualTo(round2 - round1);
        assertThat(relStrength).isEqualTo(3);
    }

    @Test
    void testCalculateCatchCosts() {
        assertThat(bulbasaur.getCatchCosts()).isEqualTo(5);

        assertThat(bulbasaur.increaseExpPts(90)).isTrue();
        assertThat(bulbasaur.getLevel()).isEqualTo(6);
        assertThat(bulbasaur.getExPoints()).isEqualTo(0);
        assertThat(bulbasaur.getCatchCosts()).isEqualTo(7);

        assertThat(bulbasaur.increaseExpPts(450)).isTrue();
        assertThat(bulbasaur.getLevel()).isEqualTo(10);
        assertThat(bulbasaur.getExPoints()).isEqualTo(50);
        assertThat(bulbasaur.getCatchCosts()).isEqualTo(15);
    }
}

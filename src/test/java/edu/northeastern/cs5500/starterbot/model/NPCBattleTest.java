package edu.northeastern.cs5500.starterbot.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

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

    @Test
    void testGetBaseDamage1() { // Physical move with level multiplier
        bulbasaur.setLevel(7);
        double attack = bulbasaur.getAttack() * (1.0 + 0.2) * 1.2;
        double defense = charmander.getDefense() * (1.0) * 0.7;
        int damage = (int) (attack - defense);
        assertThat(NPCBattle.getBaseDamage(bulbasaur, charmander, true)).isEqualTo(damage);
    }

    @Test
    void testGetBaseDamage2() { // Special attack with level multiplier
        double attack = bulbasaur.getSpecialAttack() * (1.0) * 1.2;
        charmander.setLevel(7);
        double defense = charmander.getSpecialDefense() * (1.0 + 0.2) * 0.7;
        int damage = (int) (attack - defense);
        assertThat(NPCBattle.getBaseDamage(bulbasaur, charmander, false)).isEqualTo(damage);
    }

    @Test
    void testGetBaseDamage3() { // Floor damage
        double attack = bulbasaur.getSpecialAttack() * (1.0);
        charmander.setLevel(20);
        double defense = charmander.getSpecialDefense() * (1.0 + 1.5) * 0.65;
        int damageBeforeFloor = (int) (attack - defense);
        assertThat(damageBeforeFloor).isEqualTo(-16);
        assertThat(NPCBattle.getBaseDamage(bulbasaur, charmander, false)).isEqualTo(8);
    }
}

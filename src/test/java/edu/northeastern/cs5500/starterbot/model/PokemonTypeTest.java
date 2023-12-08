package edu.northeastern.cs5500.starterbot.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class PokemonTypeTest {

    private static final double HAVE_TYPE_ADVANTAGE = 1.5;
    private static final double HAVE_TYPE_DISADVANTAGE = 0.7;
    private static final double NO_TYPE_ADVANTAGE = 1.0;

    @Test
    void testGetMoveMultiplier1() { // Normal
        PokemonType attackType = PokemonType.NORMAL;
        PokemonType defenseType = PokemonType.BUG;
        double moveMultiplier = PokemonType.getMoveMultiplier(attackType, defenseType);
        assertThat(moveMultiplier).isEqualTo(NO_TYPE_ADVANTAGE);
    }

    @Test
    void testGetMoveMultiplier2() { // Have advantage
        PokemonType attackType = PokemonType.FIRE;
        PokemonType defenseType = PokemonType.BUG;
        double moveMultiplier = PokemonType.getMoveMultiplier(attackType, defenseType);
        assertThat(moveMultiplier).isEqualTo(HAVE_TYPE_ADVANTAGE);
    }

    @Test
    void testGetMoveMultiplier3() { // Have disadvantage
        PokemonType attackType = PokemonType.FIRE;
        PokemonType defenseType = PokemonType.WATER;
        double moveMultiplier = PokemonType.getMoveMultiplier(attackType, defenseType);
        assertThat(moveMultiplier).isEqualTo(HAVE_TYPE_DISADVANTAGE);
    }

    @Test
    void testBuildTypesWithEmoji() {
        String[] resource = {"water", "electric"};
        String[] expected = {"💧 Water", "💡 Electric"};
        String[] res = PokemonType.buildTypesWithEmoji(resource);
        assertThat(res).isEqualTo(expected);
    }
}

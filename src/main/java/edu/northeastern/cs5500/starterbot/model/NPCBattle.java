package edu.northeastern.cs5500.starterbot.model;

import java.util.Random;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NPCBattle {

    private static final int DAMAGE_FLOOR = 5;
    private static final int BASE_LEVEL = 5;
    private static final double LEVEL_MULTIPLIER_BASE = 0.1;

    Pokemon trPokemon;
    Pokemon npcPokemon;
    PokemonSpecies trPokeSpecies;
    PokemonSpecies npcPokeSpecies;
    @Builder.Default boolean gameOver = Boolean.FALSE;
    @Builder.Default BattleRecord battleRecord = BattleRecord.builder().build();

    public void startBattle() {
        // Set current HP to max HP
        trPokemon.setCurrentHp(trPokemon.getHp());
        npcPokemon.setCurrentHp(npcPokemon.getHp());

        // Determine first mover
        Pokemon attackPokemon = this.getFirstAttacker();
        Pokemon defensePokemon = attackPokemon.equals(trPokemon) ? npcPokemon : trPokemon;

        while (!gameOver) {
            // Variables for ease of access
            PokemonSpecies attackSpecies =
                    attackPokemon.equals(trPokemon) ? trPokeSpecies : npcPokeSpecies;
            PokemonSpecies defenseSpecies =
                    defensePokemon.equals(npcPokemon) ? npcPokeSpecies : trPokeSpecies;

            // Determine the random Pokémon types used for this round
            PokemonType attackType = attackSpecies.getRandomType();
            PokemonType defenseType = defenseSpecies.getRandomType();

            // Determine whether to use Physical or Special attack
            boolean isPhysicalMove = new Random().nextBoolean();

            // Calculate damage and update HP
            int baseDamage = getBaseDamage(attackPokemon, defensePokemon, isPhysicalMove);
            double multiplier = PokemonType.getMoveMultiplier(attackType, defenseType);
            int finalDamage = (int) (baseDamage * multiplier);
            int newHP = defensePokemon.getCurrentHp() - finalDamage;
            defensePokemon.setCurrentHp(newHP < 0 ? 0 : newHP);

            // Generate round message
            this.recordRoundMessage(
                    attackType.getEmoji() + " " + attackSpecies.getName(),
                    defenseSpecies.getName(),
                    defenseType.getEmoji(),
                    isPhysicalMove,
                    multiplier,
                    finalDamage,
                    defensePokemon.getCurrentHp());

            // Check if the game ends or not
            if (trPokemon.getCurrentHp() <= 0) {
                this.updateFinalResult(false);
                gameOver = true;
            } else if (npcPokemon.getCurrentHp() <= 0) {
                this.updateFinalResult(true);
                gameOver = true;
            } else { // Swith attacker and defenser
                Pokemon temp = attackPokemon;
                attackPokemon = defensePokemon;
                defensePokemon = temp;
            }
        }
    }

    private Pokemon getFirstAttacker() {
        // Idea 1: If following the random logic
        // Random random = new Random();
        // return random.nextBoolean() ? trPokemon : npcPokemon;

        // Idea 2: If following the speed logic
        return trPokemon.getSpeed() >= npcPokemon.getSpeed() ? trPokemon : npcPokemon;
    }

    private static int getBaseDamage(Pokemon attacker, Pokemon defender, boolean isPhysicalMove) {
        double attack = isPhysicalMove ? attacker.getAttack() : attacker.getSpecialAttack();
        attack *= 1.0 + LEVEL_MULTIPLIER_BASE * (attacker.getLevel() - BASE_LEVEL);
        double defense = isPhysicalMove ? defender.getDefense() : defender.getSpecialDefense();
        defense *= 1.0 + LEVEL_MULTIPLIER_BASE * (defender.getLevel() - BASE_LEVEL);
        return (attack - defense < DAMAGE_FLOOR) ? DAMAGE_FLOOR : (int) (attack - defense);
    }

    private void recordRoundMessage(
            String attackerNameType,
            String defenderName,
            String defenderType,
            boolean isPhysicalMove,
            double multiplier,
            int finalDamage,
            int curHp) {
        String attackTypeStr = isPhysicalMove ? " physical attack" : "special attack";
        String effectiveness;
        if (multiplier > 1.0) effectiveness = "super effective";
        else if (multiplier < 1.0) effectiveness = "not very effective";
        else effectiveness = "effective";
        String msg =
                String.format(
                        "%s's %s is %s against %s %s, dealing %d damage! %s's current HP is %d",
                        attackerNameType,
                        attackTypeStr,
                        effectiveness,
                        defenderType,
                        defenderName,
                        finalDamage,
                        defenderName,
                        curHp);
        this.battleRecord.addBattleRoundInfo(msg);
    }

    private void updateFinalResult(boolean trainerWins) {
        this.battleRecord.updateFinalResult(trPokemon, npcPokemon, trainerWins);
    }
}

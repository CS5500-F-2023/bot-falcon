package edu.northeastern.cs5500.starterbot.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Represents Pokemon data from JSON resource file. */
@Data
@AllArgsConstructor
public class PokemonData {
    String name;
    Map<String, String> speciesNames;
    Map<String, String> formNames;
    Integer number;
    String spriteURL;
    String shinySpriteURL;
    Integer hp;
    Integer attack;
    Integer defense;
    Integer spAttack;
    Integer spDefense;
    Integer speed;
    String id;
    String[] types;

    /**
     * Checks if the PokemonData object has any null fields when loading data.
     *
     * @return true if any of the required fields are null, false otherwise.
     */
    public boolean hasNullFields() {
        return number == null
                || speciesNames.get("en") == null
                || spriteURL == null
                || types == null;
    }

    /**
     * Checks if the PokemonData object has any empty fields when loading data.
     *
     * @return true if any of the required fields are empty, false otherwise.
     */
    public boolean hasEmptyFields() {
        return speciesNames.get("en").isEmpty() || spriteURL.isEmpty() || types.length == 0;
    }

    /**
     * Checks if the PokemonData object has any null stats when loading data.
     *
     * @return true if any of the stats fields are null, false otherwise.
     */
    public boolean hasNullStats() {
        return hp == null
                || attack == null
                || defense == null
                || spAttack == null
                || spDefense == null
                || speed == null;
    }
}

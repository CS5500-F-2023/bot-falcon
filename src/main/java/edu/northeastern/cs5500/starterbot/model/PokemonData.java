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

    public boolean hasNullFields() {
        return number == null
                || speciesNames.get("en") == null
                || spriteURL == null
                || types == null;
    }

    public boolean hasEmptyFields() {
        return speciesNames.get("en").isEmpty() || spriteURL.isEmpty() || types.length == 0;
    }
}

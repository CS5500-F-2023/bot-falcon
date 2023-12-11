package edu.northeastern.cs5500.starterbot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Getter;

public enum PokemonType {
    FIRE("Fire", "🔥", 0xFF4500), // Orange Red
    WATER("Water", "💧", 0x1E90FF), // Dodger Blue
    GRASS("Grass", "🌿", 0x32CD32), // Lime Green
    NORMAL("Normal", "🧩", 0xC0C0C0), // Silver
    FIGHTING("Fighting", "🥊", 0x8B0000), // Dark Red
    FLYING("Flying", "🕊️", 0x87CEFA), // Light Sky Blue
    ROCK("Rock", "🪨", 0xA52A2A), // Brown
    BUG("Bug", "🐛", 0x6B8E23), // Olive Drab
    ELECTRIC("Electric", "💡", 0xFFD700), // Gold
    GROUND("Ground", "🌍", 0xD2B48C), // Tan
    POISON("Poison", "💀", 0x800080), // Purple
    PSYCHIC("Psychic", "🔮", 0xEE82EE), // Violet
    GHOST("Ghost", "👻", 0x663399), // Rebecca Purple
    DARK("Dark", "🌑", 0x000000), // Black
    FAIRY("Fairy", "🧚", 0xFFB6C1), // Light Pink
    STEEL("Steel", "🛡️", 0x708090), // Slate Gray
    ICE("Ice", "🧊", 0xADD8E6), // Light Blue
    DRAGON("Dragon", "🐉", 0xFFA500); // Orange

    // Define the Pokemon type advantage system
    private static EnumMap<PokemonType, ArrayList<PokemonType>> typeAdvantageMap =
            new EnumMap<>(PokemonType.class);

    static {
        typeAdvantageMap.put(FIRE, new ArrayList<>(Arrays.asList(GRASS, BUG, STEEL, ICE, FAIRY)));
        typeAdvantageMap.put(WATER, new ArrayList<>(Arrays.asList(FIRE, GROUND, ROCK)));
        typeAdvantageMap.put(GRASS, new ArrayList<>(Arrays.asList(WATER, GROUND, ROCK)));
        typeAdvantageMap.put(NORMAL, new ArrayList<>()); // Normal isn't strong against any type
        typeAdvantageMap.put(FLYING, new ArrayList<>(Arrays.asList(FIGHTING, BUG, GRASS)));
        typeAdvantageMap.put(ROCK, new ArrayList<>(Arrays.asList(FIRE, ICE, FLYING, BUG)));
        typeAdvantageMap.put(BUG, new ArrayList<>(Arrays.asList(GRASS, PSYCHIC, DARK)));
        typeAdvantageMap.put(ELECTRIC, new ArrayList<>(Arrays.asList(WATER, FLYING)));
        typeAdvantageMap.put(POISON, new ArrayList<>(Arrays.asList(GRASS, FAIRY)));
        typeAdvantageMap.put(PSYCHIC, new ArrayList<>(Arrays.asList(FIGHTING, POISON)));
        typeAdvantageMap.put(GHOST, new ArrayList<>(Arrays.asList(PSYCHIC, GHOST)));
        typeAdvantageMap.put(DARK, new ArrayList<>(Arrays.asList(PSYCHIC, GHOST)));
        typeAdvantageMap.put(FAIRY, new ArrayList<>(Arrays.asList(FIGHTING, DRAGON, DARK)));
        typeAdvantageMap.put(STEEL, new ArrayList<>(Arrays.asList(ICE, ROCK, FAIRY)));
        typeAdvantageMap.put(ICE, new ArrayList<>(Arrays.asList(GRASS, GROUND, FLYING, DRAGON)));
        typeAdvantageMap.put(DRAGON, new ArrayList<>(Arrays.asList(DRAGON)));
        typeAdvantageMap.put(
                FIGHTING, new ArrayList<>(Arrays.asList(NORMAL, ROCK, STEEL, ICE, DARK)));
        typeAdvantageMap.put(
                GROUND, new ArrayList<>(Arrays.asList(FIRE, ELECTRIC, POISON, ROCK, STEEL)));
    }

    private static final double HAVE_TYPE_ADVANTAGE = 1.5;
    private static final double HAVE_TYPE_DISADVANTAGE = 0.7;
    private static final double NO_TYPE_ADVANTAGE = 1.0;

    @Nonnull @Getter String name;

    @Nonnull @Getter String emoji;

    @Nonnull @Getter Integer color;

    PokemonType(@Nonnull String name, @Nonnull String emoji, @Nonnull Integer color) {
        this.name = name;
        this.emoji = emoji;
        this.color = color;
    }

    public static PokemonType[] getSingleTypeArray(PokemonType type) {
        PokemonType[] types = new PokemonType[1];
        types[0] = type;
        return types;
    }

    public static String getTypeString(PokemonType[] types) {
        StringBuilder typeBuilder = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            typeBuilder.append(types[i].emoji).append(types[i].name);
            if (i < types.length - 1) {
                typeBuilder.append(", "); // handle multi type
            }
        }
        return typeBuilder.toString();
    }

    /** Determines move effectiveness by the given attacker type and defender type. */
    public static double getMoveMultiplier(PokemonType attackType, PokemonType defenseType) {
        boolean attackAdvantage = typeAdvantageMap.get(attackType).contains(defenseType);
        boolean defenseAdvantage = typeAdvantageMap.get(defenseType).contains(attackType);
        if (attackAdvantage && defenseAdvantage) return NO_TYPE_ADVANTAGE;
        else if (attackAdvantage) return HAVE_TYPE_ADVANTAGE;
        else if (defenseAdvantage) return HAVE_TYPE_DISADVANTAGE;
        else return NO_TYPE_ADVANTAGE;
    }

    /** Map for build type with emoji from json */
    private static final Map<String, PokemonType> typeNameMap = new HashMap<>();

    static {
        for (PokemonType type : values()) {
            typeNameMap.put(type.getName().toLowerCase(), type);
        }
    }

    /** Helper function to get PokemonType base on type name */
    private static PokemonType fromTypeName(String typeName) {
        return typeNameMap.get(typeName.toLowerCase());
    }

    /** Build a string of type name with the corresponding emoji. */
    public static String[] buildTypesWithEmoji(String[] resource) {
        List<String> result = new ArrayList<>();
        for (String s : resource) {
            PokemonType t = PokemonType.fromTypeName(s);
            if (t != null) {
                result.add(t.getEmoji() + " " + t.getName());
            }
        }
        return result.toArray(new String[0]);
    }

    /** Build an array of PokemonType. */
    public static PokemonType[] buildPokemonTypes(String[] resource) {
        List<PokemonType> result = new ArrayList<>();
        for (String s : resource) {
            PokemonType t = PokemonType.valueOf(s.trim().toUpperCase());
            if (t != null) result.add(t);
        }
        return result.toArray(new PokemonType[0]);
    }
}

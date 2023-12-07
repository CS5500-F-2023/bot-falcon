package edu.northeastern.cs5500.starterbot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public enum PokemonType {
    FIRE("Fire", "🔥"),
    WATER("Water", "💧"),
    GRASS("Grass", "🌿"),
    NORMAL("Normal", "🧩"),
    FIGHTING("Fighting", "🥊"),
    FLYING("Flying", "🕊️"),
    ROCK("Rock", "🪨"),
    BUG("Bug", "🐛"),
    ELECTRIC("Electric", "⚡"),
    GROUND("Ground", "🌍"),
    POISON("Poison", "☠️"),
    PSYCHIC("Psychic", "🔮"),
    GHOST("Ghost", "👻"),
    DARK("Dark", "🌑"),
    FAIRY("Fairy", "🧚"),
    STEEL("Steel", "🛡️"),
    ICE("Ice", "❄️"),
    DRAGON("Dragon", "🐉");

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

    @Nonnull String name;

    @Nonnull String emoji;

    PokemonType(@Nonnull String name, @Nonnull String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public String getName() {
        return this.name;
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

    /**
     * @param resource
     * @return
     */
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
}

package edu.northeastern.cs5500.starterbot.model;

import javax.annotation.Nonnull;

public enum PokemonType {
    FIRE("Fire", "🔥"),
    WATER("Water", "💧"),
    GRASS("Grass", "🌿"),
    NORMAL("Normal", "😐");

    @Nonnull String name;

    @Nonnull String emoji;

    PokemonType(@Nonnull String name, @Nonnull String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public static PokemonType[] getSingleTypeArray(PokemonType type) {
        PokemonType[] types = new PokemonType[1];
        types[0] = type;
        return types;
    }

    public static MoveEffectiveness getEffectiveness(PokemonType attackType, PokemonType[] types) {
        // TODO(zqy): implement dual-type Pokemon
        PokemonType defendType = types[0];
        switch (defendType) {
            case NORMAL:
                return MoveEffectiveness.FULL_EFFECT;
            case FIRE:
                switch (attackType) {
                    case FIRE:
                        return MoveEffectiveness.HALF_EFFECT;
                    case WATER:
                        return MoveEffectiveness.DOUBLE_EFFECT;
                    case GRASS:
                        return MoveEffectiveness.HALF_EFFECT;
                    case NORMAL:
                        return MoveEffectiveness.FULL_EFFECT;
                }
                break;
            case WATER:
                switch (attackType) {
                    case FIRE:
                        return MoveEffectiveness.HALF_EFFECT;
                    case WATER:
                        return MoveEffectiveness.HALF_EFFECT;
                    case GRASS:
                        return MoveEffectiveness.DOUBLE_EFFECT;
                    case NORMAL:
                        return MoveEffectiveness.FULL_EFFECT;
                }
                break;
            case GRASS:
                switch (attackType) {
                    case FIRE:
                        return MoveEffectiveness.DOUBLE_EFFECT;
                    case WATER:
                        return MoveEffectiveness.HALF_EFFECT;
                    case GRASS:
                        return MoveEffectiveness.HALF_EFFECT;
                    case NORMAL:
                        return MoveEffectiveness.FULL_EFFECT;
                }
                break;
        }
        throw new IllegalStateException();
    }
}

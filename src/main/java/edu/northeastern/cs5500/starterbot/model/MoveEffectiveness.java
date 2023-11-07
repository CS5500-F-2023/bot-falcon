package edu.northeastern.cs5500.starterbot.model;

import com.mongodb.lang.Nullable;
import lombok.Getter;

public enum MoveEffectiveness {
    NO_EFFECT("NO_EFFECT(text;\"It has no effect!", 0),
    QUARTER_EFFECT("It is not very effective...", 0.25),
    HALF_EFFECT("It is not very effective...", 0.5),
    FULL_EFFECT(null, 1),
    DOUBLE_EFFECT("It is super effective!", 2),
    QUAD_EFFECT("It is super effective!", 4);

    @Nullable @Getter String text;

    double effectiveness;

    MoveEffectiveness(String text, double effectiveness) {
        this.text = text;
        this.effectiveness = effectiveness;
    }
}

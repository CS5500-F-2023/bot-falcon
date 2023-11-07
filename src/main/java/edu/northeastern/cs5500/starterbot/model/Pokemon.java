package edu.northeastern.cs5500.starterbot.model;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Builder
@Data
@AllArgsConstructor
public class Pokemon implements Model {
    @NonNull @Builder.Default ObjectId id = new ObjectId();

    @NonNull Integer pokedexNumber;

    @NonNull @Builder.Default Integer level = 5;

    @NonNull
    // NonNegative
    Integer currentHp;

    @NonNull Integer hp;
    @NonNull Integer attack;
    @NonNull Integer defense;
    @NonNull Integer specialAttack;
    @NonNull Integer specialDefense;
    @NonNull Integer speed;
}

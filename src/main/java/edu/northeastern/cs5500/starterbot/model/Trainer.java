package edu.northeastern.cs5500.starterbot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Builder
@Data
public class Trainer implements Model {
    // Model ID e.g. for MongoDB
    ObjectId id;

    // This is the "snowflake id" of the user
    // e.g. event.getUser().getId()
    String discordUserId;

    @Builder.Default @Nonnull @Nonnegative Integer balance = 10;

    @Builder.Default List<ObjectId> pokemonInventory = new ArrayList<>();

    @Builder.Default Map<FoodType, Integer> foodInventory = new HashMap<>();
}

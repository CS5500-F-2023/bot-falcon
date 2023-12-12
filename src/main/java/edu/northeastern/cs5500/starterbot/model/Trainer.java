package edu.northeastern.cs5500.starterbot.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trainer implements Model {
    // Model ID e.g. for MongoDB
    ObjectId id;

    // This is the "snowflake id" of the user
    // e.g. event.getUser().getId()
    String discordUserId;

    @Builder.Default @Nonnull
    LocalDate lastCheckIn =
            LocalDate.now().minusDays(1); // This allows new user to user /daily once

    @Builder.Default @Nonnull @Nonnegative Integer balance = 10;

    @Builder.Default List<ObjectId> pokemonInventory = new ArrayList<>();

    @Builder.Default Map<String, Integer> foodInventory = new HashMap<>();

    @Builder.Default Map<String, ObjectId> indexToObjectIDMap = new HashMap<>(); // for mongodb
}

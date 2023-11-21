package edu.northeastern.cs5500.starterbot.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @Builder.Default @Nonnull
    LocalDate lastCheckIn =
            LocalDate.now().minusDays(1); // This allows new user to user /daily once

    @Builder.Default @Nonnull @Nonnegative Integer balance = 10;

    @Builder.Default List<ObjectId> pokemonInventory = new ArrayList<>();
}

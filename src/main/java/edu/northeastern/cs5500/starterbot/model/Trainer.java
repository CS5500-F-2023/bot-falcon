package edu.northeastern.cs5500.starterbot.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Trainer implements Model {
    // Model ID e.g. for MongoDB
    ObjectId id;

    // This is the "snowflake id" of the user
    // e.g. event.getUser().getId()
    String discordUserId;

    List<ObjectId> pokemonInventory = new ArrayList<>();
}

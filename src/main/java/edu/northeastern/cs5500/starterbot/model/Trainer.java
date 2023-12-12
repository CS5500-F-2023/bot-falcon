package edu.northeastern.cs5500.starterbot.model;

import edu.northeastern.cs5500.starterbot.exception.InvalidPokemonException;
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

    private static final String BOARD_LINE = "----------------------------\n";
    private static final Integer MIN_BALANCE = 10;

    /**
     * Retrieves the ID of the trainer's Pokémon based on the given inventory index.
     *
     * @param inventoryIndex the index of the Pokémon in the trainer's inventory
     * @return the ID of the Pokémon
     * @throws InvalidPokemonException if the trainer's inventory is empty or the index is invalid
     */
    public String getTrainerPokemonIdByIndex(int inventoryIndex) throws InvalidPokemonException {
        if (this.indexToObjectIDMap.containsKey(Integer.toString(inventoryIndex))) {
            return this.indexToObjectIDMap.get(Integer.toString(inventoryIndex)).toString();
        } else {
            throw new InvalidPokemonException("Invalid index.");
        }
    }

    /**
     * Builds the trainer statistics for a given Discord member ID.
     *
     * @return a formatted string containing the trainer statistics
     */
    public String buildTrainerStats() {
        String foodDetail = buildTrainerBerryStockDetail();
        StringBuilder statsBuilder = new StringBuilder();

        /** build basic stats */
        statsBuilder.append("📊 Your Stats 📊\n");
        statsBuilder.append(BOARD_LINE);
        statsBuilder.append("   Balance         💰 : ").append(this.getBalance()).append("\n");
        statsBuilder
                .append("   Pokemon Numbers 🎒 : ")
                .append(this.getPokemonInventory().size())
                .append("\n");
        statsBuilder.append("\n🍇 Your Berry Inventory 🍇\n");
        statsBuilder.append(BOARD_LINE);
        statsBuilder.append(foodDetail).append("\n");

        /** customize hint base on stats */
        if (!this.getPokemonInventory().isEmpty()) {
            statsBuilder.append("🔍 Explore your Pokemon inventory with /pokemon!\n");
        } else {
            statsBuilder.append("🐣 Use /spawn to discover and catch new Pokemon!\n");
        }
        if (this.getBalance() <= MIN_BALANCE) {
            statsBuilder.append(
                    "💵 Boost your balance by claiming your daily rewards with /daily!\n");
        }
        statsBuilder.append("😋 Refill your berry stock at the shop using /shop!");

        return "```" + statsBuilder.toString() + "```";
    }

    /**
     * Builds a string representation of the trainer's berry stock details.
     *
     * @return a string representation of the trainer's berry stock details
     */
    private String buildTrainerBerryStockDetail() {
        StringBuilder sb = new StringBuilder();
        for (FoodType food : FoodType.values()) {
            sb.append(
                    String.format(
                            "   %-15s %s : %d\n",
                            food.getName(),
                            food.getEmoji(),
                            this.foodInventory.getOrDefault(food.getUppercaseName(), 0)));
        }
        return sb.toString();
    }
}

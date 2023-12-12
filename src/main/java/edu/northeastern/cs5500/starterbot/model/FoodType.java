package edu.northeastern.cs5500.starterbot.model;

import javax.annotation.Nonnull;

public enum FoodType {
    MYSTERYBERRY("Mystery Berry", 5, 5, "🍭"),
    BERRY("Berry", 10, 10, "🫐"),
    GOLDBERRY("Gold Berry", 30, 30, "🌟");

    @Nonnull String name;

    @Nonnull Integer price;

    @Nonnull Integer exp;

    @Nonnull String emoji;

    FoodType(
            @Nonnull String name,
            @Nonnull Integer price,
            @Nonnull Integer exp,
            @Nonnull String emoji) {
        this.name = name;
        this.price = price;
        this.exp = exp;
        this.emoji = emoji;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getExp() {
        return exp;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    /** e.g "Gold Berry" will be format as "GOLDBERRY" */
    public String getUppercaseName() {
        return name.replaceAll("\\s", "").toUpperCase();
    }
}

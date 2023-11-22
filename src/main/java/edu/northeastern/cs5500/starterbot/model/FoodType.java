package edu.northeastern.cs5500.starterbot.model;

import javax.annotation.Nonnull;

public enum FoodType {
    MYSTERYBERRY("Mystery Berry", 5),
    BERRY("Berry", 10),
    GOLDBERRY("Gold Berry", 30);

    @Nonnull String name;

    @Nonnull Integer price;

    FoodType(@Nonnull String name, @Nonnull Integer price) {
        this.name = name;
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }
}

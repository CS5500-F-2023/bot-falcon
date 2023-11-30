package edu.northeastern.cs5500.starterbot.model;

import javax.annotation.Nonnull;

public enum FoodType {
    MYSTERYBERRY("Mystery Berry", 5, 5),
    BERRY("Berry", 10, 10),
    GOLDBERRY("Gold Berry", 30, 30);

    @Nonnull String name;

    @Nonnull Integer price;

    @Nonnull Integer exp;

    FoodType(@Nonnull String name, @Nonnull Integer price, @Nonnull Integer exp) {
        this.name = name;
        this.price = price;
        this.exp = exp;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getExp() {
        return exp;
    }
}

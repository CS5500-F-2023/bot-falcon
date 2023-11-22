package edu.northeastern.cs5500.starterbot.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Shop {

    @Builder.Default Map<FoodType, Integer> foodPrice = new HashMap<>();
}

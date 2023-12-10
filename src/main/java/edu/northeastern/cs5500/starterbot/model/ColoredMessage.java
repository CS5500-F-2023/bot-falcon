package edu.northeastern.cs5500.starterbot.model;

import lombok.Data;

@Data
public class ColoredMessage {
    String message;
    int color;

    public ColoredMessage(String message, int color) {
        this.message = message;
        this.color = color;
    }
}

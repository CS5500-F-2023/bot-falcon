package edu.northeastern.cs5500.starterbot.model;

public class BotConstants {

    private BotConstants() {
        throw new AssertionError("Cannot instantiate this class");
    }

    public static final int COLOR_TRAINER = 0x87CEEB; // trainer's color: sky blue
    public static final int COLOR_NPC = 0xDC143C; // npc's color: crimson red
    public static final int COLOR_WARNING = 0xF4B431; // yellow, same as discord notification
    public static final int COLOR_SUCCESS = 0x5CA266; // green, same as discord success

    public static final int POKE_DEFAULT_LEVEL = 5;
    public static final int POKE_DEFAULT_XP = 10;
    public static final int POKE_LEVEL_UP_THRESHOLD = 100;

    public static final int COST_PER_BATTLE = 5;
}

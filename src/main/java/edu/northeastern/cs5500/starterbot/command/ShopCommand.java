package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Slf4j
public class ShopCommand implements SlashCommandHandler, StringSelectHandler {

    static final String NAME = "shop";

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    public ShopCommand() {
        // Defined public and empty for Dagger injection
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {
        return Commands.slash(getName(), "A shop for trainer to purchase food.");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /shop");

        String trainerDiscordId = event.getMember().getId();

        // Dropdown options
        List<SelectOption> options = new ArrayList<>();
        for (FoodType foodType : FoodType.values()) {
            options.add(
                    SelectOption.of(
                            foodType.getEmoji()
                                    + " "
                                    + foodType.getName()
                                    + ": "
                                    + foodType.getPrice()
                                    + " coins",
                            foodType.getName() + ":" + trainerDiscordId));
        }

        SelectMenu menu =
                StringSelectMenu.create("shop")
                        .setPlaceholder("Choose the food type")
                        .addOptions(options)
                        .build();

        event.reply("🛍️ Welcome to the Berry Shop! Choose the type of food you want to buy.")
                .addActionRow(menu)
                .queue();
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        final String response = event.getInteraction().getValues().get(0);

        String[] fields = response.split(":");
        String selectedFoodTypeResponse = fields[0];
        String initiateTrainerDiscordId = fields[1];
        String trainerDiscordId = event.getMember().getId();
        FoodType selectedFoodType = null;

        for (FoodType foodType : FoodType.values()) {
            if (selectedFoodTypeResponse.equalsIgnoreCase(foodType.getName())) {
                selectedFoodType = foodType;
                break;
            }
        }

        if (selectedFoodType != null) {
            if (trainerDiscordId.equals(initiateTrainerDiscordId)) {
                try {
                    trainerController.decreaseTrainerBalance(
                            trainerDiscordId, selectedFoodType.getPrice());
                    trainerController.addTrainerFood(trainerDiscordId, selectedFoodType);
                    event.reply(
                                    String.format(
                                            "<@%s>, you bought a %s !",
                                            trainerDiscordId, selectedFoodTypeResponse))
                            .queue();
                } catch (InsufficientBalanceException e) {
                    event.reply(
                                    String.format(
                                            "<@%s>, you don't have enough coins to buy the %s!",
                                            trainerDiscordId, selectedFoodTypeResponse))
                            .queue();
                }
            } else {
                event.reply(
                                String.format(
                                        "Sorry <@%s>, you are not authorized to perform this action.",
                                        trainerDiscordId))
                        .queue();
            }
        } else {
            event.reply("Invalid food type selected.").queue();
        }
    }
}

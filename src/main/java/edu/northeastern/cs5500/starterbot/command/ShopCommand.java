package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientBalanceException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Slf4j
public class ShopCommand implements SlashCommandHandler, StringSelectHandler {

    static final String NAME = "shop";

    @Inject
    PokemonController pokemonController;

    @Inject
    PokedexController pokedexController;

    @Inject
    TrainerController trainerController;

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
        // Dropdown
        StringSelectMenu menu = StringSelectMenu.create("shop")
                .setPlaceholder("Choose the food type") // shows the placeholder
                // indicating what this
                // menu is for
                .addOption("Mystery Berry: 5 coins", "MYSTERYBERRY:" + trainerDiscordId)
                .addOption("Berry: 10 coins", "BERRY:" + trainerDiscordId)
                .addOption("Gold Berry: 30 coins", "GOLDBERRY:" + trainerDiscordId)
                .build();

        event.reply("Welcome to the Berry Shop! Choose the type of food you want to buy.")
                .setEphemeral(true)
                .addActionRow(menu)
                // .addActionRow(
                // Button.success(getName() + ":buy:" + trainerDiscordId, "Buy"),
                // Button.danger(getName() + ":cancel:" + trainerDiscordId, "Cancel"))
                .queue();
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        final String response = event.getInteraction().getValues().get(0);
        System.out.println(response);
        String[] fields = response.split(":");
        System.out.println(fields);
        String selectedFoodTypeResponse = fields[0];
        System.out.println(selectedFoodTypeResponse);
        String initiateTrainerDiscordId = fields[1];
        System.out.println(initiateTrainerDiscordId);
        String trainerDiscordId = event.getMember().getId();
        FoodType selectedFoodType;
        Objects.requireNonNull(response);

        if (response.equals("MYSTERYBERRY")) {
            selectedFoodType = FoodType.MYSTERYBERRY;
        } else if (response.equals("BERRY")) {
            selectedFoodType = FoodType.BERRY;
        } else {
            selectedFoodType = FoodType.GOLDBERRY;
        }
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
        event.reply(response).queue();
    }
}

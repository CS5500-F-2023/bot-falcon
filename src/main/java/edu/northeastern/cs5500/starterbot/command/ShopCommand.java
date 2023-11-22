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
    StringSelectMenu menu;

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
        // Dropdown
        menu =
                StringSelectMenu.create("shop")
                        .setPlaceholder("Choose the food type") // shows the placeholder
                        // indicating what this
                        // menu is for
                        .addOption("🍭 Mystery Berry: 5 coins", "Mystery Berry:" + trainerDiscordId)
                        .addOption("🫐 Berry: 10 coins", "Berry:" + trainerDiscordId)
                        .addOption("🌟 Gold Berry: 30 coins", "Gold Berry:" + trainerDiscordId)
                        .build();

        event.reply("🛍️ Welcome to the Berry Shop! Choose the type of food you want to buy.")
                .setEphemeral(true)
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
        FoodType selectedFoodType = FoodType.MYSTERYBERRY;
        Objects.requireNonNull(response);

        if (selectedFoodTypeResponse.equals("Mystery Berry")) {
            selectedFoodType = FoodType.MYSTERYBERRY;
        } else if (selectedFoodTypeResponse.equals("Berry")) {
            selectedFoodType = FoodType.BERRY;
        } else if (selectedFoodTypeResponse.equals("Gold Berry")) {
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

package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientFoodException;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class FeedCommand implements SlashCommandHandler, ButtonHandler {

        static final String NAME = "feed";

        @Inject
        TrainerController trainerController;
        @Inject
        PokemonController pokemonController;
        @Inject
        PokedexController pokedexController;

        @Inject
        public FeedCommand() {
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
                return Commands.slash(
                                getName(), "Choose the Pokemon you want to feed by typing its number!")
                                .addOption(
                                                OptionType.INTEGER,
                                                "pokemon",
                                                "The bot will reply with food options in your food inventory",
                                                true);
        }

        @Override
        public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
                log.info("event: /feed");
                try {
                        String trainerDiscordId = event.getUser().getId();
                        Integer pokemonInventoryIndex = event.getOption("pokemon").getAsInt() - 1;
                        Pokemon pokemon = trainerController.getPokemonFromInventory(
                                        trainerDiscordId, pokemonInventoryIndex);
                        PokemonSpecies species = pokedexController
                                        .getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());

                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setThumbnail(species.getImageUrl());
                        embedBuilder.setTitle("Choose the berry to level up your Pokemon!");

                        // embedBuilder.addField(
                        // "Your Selected Pokemon's Current Level:",
                        // "Level: " + pokemon.getLevel().toString(),
                        // false);
                        // embedBuilder.setDescription(String.format("Here is your food inventory"));
                        embedBuilder.setDescription(
                                        String.format(
                                                        "Your Selected Pokemon's Current Level: %s",
                                                        pokemon.getLevel().toString()));
                        embedBuilder.addField(
                                        "----\n🎒 Below is your food inventory!\n----",
                                        "💡Not enough berries? Type /shop to buy more berries!",
                                        false);
                        // event.replyEmbeds(embedBuilder.build()).queue();

                        // EmbedBuilder embedBuilderFood = new EmbedBuilder();
                        // embedBuilderFood.setTitle(String.format("Choose the berry to level up your
                        // Pokemon!"));
                        // embedBuilderFood.setDescription(String.format("Here is your food
                        // inventory"));

                        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                        messageCreateBuilder = messageCreateBuilder.addActionRow(
                                        Button.primary(
                                                        getName()
                                                                        + ":mysteryberry:"
                                                                        + pokemon.getId().toString()
                                                                        + ":"
                                                                        + trainerDiscordId,
                                                        "🍭Mystery Berry🍭     "
                                                                        + trainerController
                                                                                        .getTrainerFoodInventory(
                                                                                                        trainerDiscordId)
                                                                                        .get(FoodType.MYSTERYBERRY)
                                                                                        .toString()),
                                        Button.primary(
                                                        getName()
                                                                        + ":berry:"
                                                                        + pokemon.getId().toString()
                                                                        + ":"
                                                                        + trainerDiscordId,
                                                        "🫐Berry🫐     "
                                                                        + trainerController
                                                                                        .getTrainerFoodInventory(
                                                                                                        trainerDiscordId)
                                                                                        .get(FoodType.BERRY)
                                                                                        .toString()),
                                        Button.primary(
                                                        getName()
                                                                        + ":goldberry:"
                                                                        + pokemon.getId().toString()
                                                                        + ":"
                                                                        + trainerDiscordId,
                                                        "🌟Gold Berry🌟     "
                                                                        + trainerController
                                                                                        .getTrainerFoodInventory(
                                                                                                        trainerDiscordId)
                                                                                        .get(FoodType.GOLDBERRY)
                                                                                        .toString()));
                        messageCreateBuilder = messageCreateBuilder.addEmbeds(embedBuilder.build());
                        event.reply(messageCreateBuilder.build()).queue();
                } catch (InvalidInventoryIndexException e) {
                        event.reply("Oops...the pokemon does not exist, try again").queue();
                }
        }

        @Override
        public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
                String[] buttonIdParts = event.getButton().getId().split(":");
                String action = buttonIdParts[1]; // "mystery berry" or "berry" or "gold berry"
                String pokemonID = buttonIdParts[2];
                String initiateTrainerDiscordId = buttonIdParts[3];
                String trainerDiscordId = event.getMember().getId();
                Pokemon pokemon = pokemonController.getPokemonById(pokemonID);
                MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
                PokemonSpecies species = pokedexController.getPokemonSpeciesByPokedex(pokemon.getPokedexNumber());
                FoodType selectedFoodType = FoodType.MYSTERYBERRY;
                if (action.equals("mysteryberry")) {
                        selectedFoodType = FoodType.MYSTERYBERRY;
                } else if (action.equals("berry")) {
                        selectedFoodType = FoodType.BERRY;
                } else if (action.equals("goldberry")) {
                        selectedFoodType = FoodType.GOLDBERRY;
                }
                // Handle the button interaction
                if (trainerDiscordId.equals(initiateTrainerDiscordId)) {
                        try {
                                trainerController.removeTrainerFood(trainerDiscordId, selectedFoodType);
                                pokemonController.increasePokemonLevelByFood(pokemon, selectedFoodType);
                                event.reply(
                                                String.format(
                                                                "<@%s>, you leveled up your %s !",
                                                                trainerDiscordId, species.getName()))
                                                .queue();
                                event.getMessage()
                                                .editMessageEmbeds(messageEmbed)
                                                .setComponents()
                                                .queue(); // disable button
                        } catch (InsufficientFoodException e) {
                                event.reply(
                                                String.format(
                                                                "<@%s>, you don't have enough %s to level up your %s!",
                                                                trainerDiscordId, action, species.getName()))
                                                .queue();
                                event.getMessage().editMessageEmbeds(messageEmbed).setComponents().queue();
                        }
                } else {
                        event.reply(
                                        String.format(
                                                        "Sorry <@%s>, you are not authorized to perform this action.",
                                                        trainerDiscordId))
                                        .queue();
                        event.getMessage()
                                        .editMessageEmbeds(messageEmbed)
                                        .setComponents()
                                        .queue(); // disable button
                }
        }
}

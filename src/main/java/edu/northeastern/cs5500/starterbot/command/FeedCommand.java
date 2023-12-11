package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.PokemonEvolutionController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.exception.InsufficientFoodException;
import edu.northeastern.cs5500.starterbot.exception.InvalidInventoryIndexException;
import edu.northeastern.cs5500.starterbot.model.FoodType;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import java.util.Map;
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
    private static final Integer LEVEL_UP_THRESHOLD = 100;
    private static final Integer LEVEL_UP_HINT_THRESHOLD = 75;
    String boardline = "---------------------------------------------\n";

    @Inject TrainerController trainerController;
    @Inject PokemonController pokemonController;
    @Inject PokedexController pokedexController;
    @Inject PokemonEvolutionController pokemonEvolutionController;

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
            Pokemon pokemon =
                    trainerController.getPokemonFromInventory(
                            trainerDiscordId, pokemonInventoryIndex);
            PokemonSpecies species =
                    pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
            if (foodInventoryIsEmpty(trainerDiscordId)) {
                event.reply(
                                "Oops...your food inventory is empty.\nType `/shop` to buy more berries!")
                        .queue();
            } else {

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setThumbnail(species.getImageUrl());
                embedBuilder.setTitle("Choose the berry to increse your Pokemon's XP!");
                embedBuilder.setDescription(
                        String.format(
                                "```Your Selected Pokemon's Info:\n Current Level: %s\n Current XP: %s```",
                                pokemon.getLevel().toString(), pokemon.getExPoints().toString()));
                embedBuilder.addField(
                        boardline,
                        String.format(
                                "```🎒 Below is your food inventory!\n💡 Not enough berries? Type `/shop` to buy more berries!```"),
                        false);

                MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                for (FoodType foodType : FoodType.values()) {
                    messageCreateBuilder.addActionRow(
                            createFoodTypeButton(foodType, pokemon, trainerDiscordId));
                }
                messageCreateBuilder = messageCreateBuilder.addEmbeds(embedBuilder.build());
                event.reply(messageCreateBuilder.build()).queue();
            }

        } catch (InvalidInventoryIndexException e) {
            event.reply("Oops...the pokemon does not exist, try again").queue();
        }
    }

    private Button createFoodTypeButton(
            FoodType foodType, Pokemon pokemon, String trainerDiscordId) {
        return Button.primary(
                getName()
                        + ":"
                        + foodType.getName()
                        + ":"
                        + pokemon.getId().toString()
                        + ":"
                        + trainerDiscordId,
                foodType.getEmoji()
                        + " "
                        + foodType.getName()
                        + ": "
                        + trainerController
                                .getTrainerFoodInventory(trainerDiscordId)
                                .get(foodType)
                                .toString());
    }

    private Boolean foodInventoryIsEmpty(String trainerDiscordID) {
        Map<FoodType, Integer> foodInventory =
                trainerController.getTrainerFoodInventory(trainerDiscordID);
        for (Integer count : foodInventory.values()) {
            if (count >= 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String[] buttonIdParts = event.getButton().getId().split(":");
        String selectedFoodTypeResponse = buttonIdParts[1];
        String pokemonID = buttonIdParts[2];
        String initiateTrainerDiscordId = buttonIdParts[3];
        String trainerDiscordId = event.getMember().getId();
        Pokemon pokemon = pokemonController.getPokemonById(pokemonID);
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        PokemonSpecies species =
                pokedexController.getPokemonSpeciesByREALPokedex(pokemon.getPokedexNumber());
        FoodType selectedFoodType = null;

        for (FoodType foodType : FoodType.values()) {
            if (selectedFoodTypeResponse.equalsIgnoreCase(foodType.getName())) {
                selectedFoodType = foodType;
                break;
            }
        }
        // Handle the button interaction
        if (trainerDiscordId.equals(initiateTrainerDiscordId)) {
            try {
                trainerController.removeTrainerFood(trainerDiscordId, selectedFoodType);
                int levelBefore = pokemon.getLevel();
                pokemonController.increasePokemonExpByFood(pokemonID, selectedFoodType);
                int newXP = pokemon.getExPoints();
                int levelAfter = pokemon.getLevel();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setDescription(
                        String.format(
                                "%s Yummy! Your %s gained %d experience points!%n",
                                selectedFoodType.getEmoji(),
                                species.getName(),
                                selectedFoodType.getExp()));
                embedBuilder.appendDescription(boardline);
                if (levelAfter > levelBefore) {
                    embedBuilder.appendDescription(
                            String.format("LEVEL UP to   📈: %d%n", levelAfter));
                } else {
                    embedBuilder.appendDescription(
                            String.format("Current Level 🌟: %d%n", levelAfter));
                }
                embedBuilder.appendDescription(
                        String.format(
                                "Current XP    🏆: %s %d/%d%n",
                                pokemon.generateXpProgressBar(),
                                pokemon.getExPoints(),
                                LEVEL_UP_THRESHOLD));
                boolean evolved = pokemonEvolutionController.evolvePokemon(pokemonID);

                if (evolved) {
                    int pokedex = pokemonController.getPokemonById(pokemonID).getPokedexNumber();
                    PokemonSpecies evolvedSpecies =
                            pokedexController.getPokemonSpeciesByREALPokedex(pokedex);
                    embedBuilder.appendDescription(
                            String.format("EVOLVED to    🚀: %s%n", evolvedSpecies.getName()));
                }
                embedBuilder.appendDescription(boardline);
                if (levelAfter > levelBefore) {
                    embedBuilder.appendDescription(
                            String.format(
                                    "🎉 Woo-hoo, your %s is leveled up to %d!%n",
                                    species.getName(), levelAfter));
                } else if (newXP >= LEVEL_UP_HINT_THRESHOLD) {
                    int xpRequiredNextLevel = LEVEL_UP_THRESHOLD - newXP;
                    embedBuilder.appendDescription(
                            String.format(
                                    "💪 Almost there! your %s only need %d more XP to level up!%n",
                                    species.getName(), xpRequiredNextLevel));
                }
                if (evolved) {
                    int pokedex = pokemonController.getPokemonById(pokemonID).getPokedexNumber();
                    PokemonSpecies evolvedSpecies =
                            pokedexController.getPokemonSpeciesByREALPokedex(pokedex);
                    embedBuilder.appendDescription(
                            String.format(
                                    "🎊 Hooray, your %s is evolved to %s!%n",
                                    species.getName(), evolvedSpecies.getName()));
                }

                event.replyEmbeds(embedBuilder.build()).queue();
                event.getMessage()
                        .editMessageEmbeds(messageEmbed)
                        .setComponents()
                        .queue(); // disable button
            } catch (InsufficientFoodException e) {
                event.reply(
                                String.format(
                                        "<@%s>, you don't have enough %s to increase your %s XP",
                                        trainerDiscordId,
                                        selectedFoodTypeResponse,
                                        species.getName()))
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

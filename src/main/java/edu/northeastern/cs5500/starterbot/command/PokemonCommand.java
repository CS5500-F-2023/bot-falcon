package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.PokedexController;
import edu.northeastern.cs5500.starterbot.controller.PokemonController;
import edu.northeastern.cs5500.starterbot.controller.TrainerController;
import edu.northeastern.cs5500.starterbot.model.Pokemon;
import edu.northeastern.cs5500.starterbot.model.PokemonSpecies;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class PokemonCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "pokemon";

    @Inject PokemonController pokemonController;

    @Inject PokedexController pokedexController;

    @Inject TrainerController trainerController;

    @Inject
    public PokemonCommand() {
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
        return Commands.slash(getName(), "View your pokemon status");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /pokemon");

        String trainerDiscordId = event.getMember().getId();

        List<Pokemon> pokemonInventory =
                trainerController.getTrainerPokemonInventory(trainerDiscordId);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Your Pokemon Inventory");
        embedBuilder.setDescription("Pick one to view detail stats!");

        // handle defualt user
        if (pokemonInventory.isEmpty()) {
            embedBuilder.addField("Oops....no Pokemon Found", "", false);
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        List<Button> pokemonButtons = new ArrayList<>();
        for (Pokemon pokemon : pokemonInventory) {
            PokemonSpecies species =
                    pokedexController.getePokemonSpeciesByNumber(pokemon.getPokedexNumber());
            pokemonButtons.add(
                    Button.primary(
                            getName()
                            + ":"
                            + pokemon.getId().toString()
                            + ":"
                            + trainerDiscordId,
                            species.getName()));
            if (pokemonButtons.size() == 3) {
                messageCreateBuilder = messageCreateBuilder.addActionRow(pokemonButtons);
                pokemonButtons.clear();
            }
        }
        if (!pokemonButtons.isEmpty()) {
            messageCreateBuilder = messageCreateBuilder.addActionRow(pokemonButtons);
        }
        messageCreateBuilder = messageCreateBuilder.addEmbeds(embedBuilder.build());
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String[] buttonIdParts = event.getButton().getId().split(":");
        String pokemonId = buttonIdParts[1];
        String initiateTrainerDiscordId = buttonIdParts[2];
        String trainerDiscordId = event.getMember().getId();


        if (trainerDiscordId.equals(initiateTrainerDiscordId)) {
            String pokemonDetails = trainerController.buildPokemonStats(trainerDiscordId, pokemonId);
            event.reply(pokemonDetails).queue();
        } else {
            event.reply(
                            String.format(
                                    "Sorry <@%s>, you are not authorized to perform this action.",
                                    trainerDiscordId))
                    .queue();
        }
    }
}

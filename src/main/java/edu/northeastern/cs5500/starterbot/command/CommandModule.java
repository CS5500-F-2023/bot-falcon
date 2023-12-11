package edu.northeastern.cs5500.starterbot.command;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public class CommandModule {

    @Provides
    @IntoMap
    @StringKey(SpawnCommand.NAME)
    public SlashCommandHandler provideSpawnCommand(SpawnCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(SpawnCommand.NAME)
    public ButtonHandler provideSpawnCommandClickHandler(SpawnCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(DailyCommand.NAME)
    public SlashCommandHandler provideDailyCommand(DailyCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(StatusCommand.NAME)
    public SlashCommandHandler provideStatusCommand(StatusCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(PokemonCommand.NAME)
    public SlashCommandHandler providePokemonCommand(PokemonCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(MyCommand.NAME)
    public SlashCommandHandler provideMyCommand(MyCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(ShopCommand.NAME)
    public SlashCommandHandler provideShopCommand(ShopCommand shopCommand) {
        return shopCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ShopCommand.NAME)
    public StringSelectHandler provideShopCommandMenuHandler(ShopCommand shopCommand) {
        return shopCommand;
    }

    @Provides
    @IntoMap
    @StringKey(BattleCommand.NAME)
    public SlashCommandHandler provideBattleCommand(BattleCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(BattleCommand.NAME)
    public StringSelectHandler provideBattleCommandMenuHandler(BattleCommand command) {
        return command;
    }

    @Provides
    @IntoMap
    @StringKey(FeedCommand.NAME)
    public SlashCommandHandler provideFeedCommand(FeedCommand feedCommand) {
        return feedCommand;
    }

    @Provides
    @IntoMap
    @StringKey(FeedCommand.NAME)
    public ButtonHandler provideFeedCommandClickHandler(FeedCommand feedCommand) {
        return feedCommand;
    }

    @Provides
    @IntoMap
    @StringKey(HelpCommand.NAME)
    public SlashCommandHandler provideHelpCommand(HelpCommand helpCommand) {
        return helpCommand;
    }
}

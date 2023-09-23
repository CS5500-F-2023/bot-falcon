package edu.northeastern.cs5500.starterbot.command;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public class CommandModule {

    @Provides
    @IntoMap
    @StringKey(SayCommand.NAME)
    public SlashCommandHandler provideSayCommand(SayCommand sayCommand) {
        return sayCommand;
    }

    @Provides
    @IntoMap
    @StringKey(PreferredNameCommand.NAME)
    public SlashCommandHandler providePreferredNameCommand(
            PreferredNameCommand preferredNameCommand) {
        return preferredNameCommand;
    }

    @Provides
    @IntoMap
    @StringKey(FailureCommand.NAME)
    public SlashCommandHandler provideFailureCommand(FailureCommand failureCommand) {
        return failureCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ButtonCommand.NAME)
    public SlashCommandHandler provideButtonCommand(ButtonCommand buttonCommand) {
        return buttonCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ButtonCommand.NAME)
    public ButtonHandler provideButtonCommandClickHandler(ButtonCommand buttonCommand) {
        return buttonCommand;
    }

    @Provides
    @IntoMap
    @StringKey(DropdownCommand.NAME)
    public SlashCommandHandler provideDropdownCommand(DropdownCommand dropdownCommand) {
        return dropdownCommand;
    }

    @Provides
    @IntoMap
    @StringKey(DropdownCommand.NAME)
    public StringSelectHandler provideDropdownCommandMenuHandler(DropdownCommand dropdownCommand) {
        return dropdownCommand;
    }
}

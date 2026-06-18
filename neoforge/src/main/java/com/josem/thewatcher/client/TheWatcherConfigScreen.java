package com.josem.thewatcher.client;

import com.josem.thewatcher.game.TheWatcherConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TheWatcherConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("The Watcher Configuration"));

        builder.setSavingRunnable(TheWatcherConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Fear Increase Multiplier"), TheWatcherConfig.fearIncreaseMultiplier())
                .setDefaultValue(1.0D)
                .setMin(0.0D).setMax(10.0D)
                .setSaveConsumer(TheWatcherConfig::setFearIncreaseMultiplier)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Fear Decrease Multiplier"), TheWatcherConfig.fearDecreaseMultiplier())
                .setDefaultValue(1.0D)
                .setMin(0.0D).setMax(10.0D)
                .setSaveConsumer(TheWatcherConfig::setFearDecreaseMultiplier)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Torch Fear Reduction"), TheWatcherConfig.torchFearReduction())
                .setDefaultValue(0.2D)
                .setMin(0.0D).setMax(1.0D)
                .setTooltip(Component.literal("Reducción de miedo al sostener antorchas. 0.2 = 20% menos de miedo."))
                .setSaveConsumer(TheWatcherConfig::setTorchFearReduction)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Fake Crash Enabled"), TheWatcherConfig.fakeCrashEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(TheWatcherConfig::setFakeCrashEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Action Echoes Enabled"), TheWatcherConfig.actionEchoesEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(TheWatcherConfig::setActionEchoesEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Item Haunting Enabled"), TheWatcherConfig.itemHauntingEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(TheWatcherConfig::setItemHauntingEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Hotbar Drift Enabled"), TheWatcherConfig.hotbarDriftEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(TheWatcherConfig::setHotbarDriftEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Environmental Events Enabled"), TheWatcherConfig.environmentalEventsEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(TheWatcherConfig::setEnvironmentalEventsEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Animal Staring Enabled"), TheWatcherConfig.animalStaringEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(TheWatcherConfig::setAnimalStaringEnabled)
                .build());

        return builder.build();
    }
}

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
                .setTitle(Component.translatable("config.thewatcher.title"));

        builder.setSavingRunnable(TheWatcherConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.thewatcher.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.thewatcher.fear_increase_multiplier"), TheWatcherConfig.fearIncreaseMultiplier())
                .setDefaultValue(1.0D)
                .setMin(0.0D).setMax(10.0D)
                .setTooltip(Component.translatable("config.thewatcher.fear_increase_multiplier.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setFearIncreaseMultiplier)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.thewatcher.fear_decrease_multiplier"), TheWatcherConfig.fearDecreaseMultiplier())
                .setDefaultValue(1.0D)
                .setMin(0.0D).setMax(10.0D)
                .setTooltip(Component.translatable("config.thewatcher.fear_decrease_multiplier.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setFearDecreaseMultiplier)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.thewatcher.torch_fear_reduction"), TheWatcherConfig.torchFearReduction())
                .setDefaultValue(0.4D)
                .setMin(0.0D).setMax(1.0D)
                .setTooltip(Component.translatable("config.thewatcher.torch_fear_reduction.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setTorchFearReduction)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.fake_crash_enabled"), TheWatcherConfig.fakeCrashEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.fake_crash_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setFakeCrashEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.action_echoes_enabled"), TheWatcherConfig.actionEchoesEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.action_echoes_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setActionEchoesEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.item_haunting_enabled"), TheWatcherConfig.itemHauntingEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.item_haunting_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setItemHauntingEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.hotbar_drift_enabled"), TheWatcherConfig.hotbarDriftEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.hotbar_drift_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setHotbarDriftEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.environmental_events_enabled"), TheWatcherConfig.environmentalEventsEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.environmental_events_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setEnvironmentalEventsEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.animal_staring_enabled"), TheWatcherConfig.animalStaringEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.animal_staring_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setAnimalStaringEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.thewatcher.shadow_enabled"), TheWatcherConfig.shadowEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.thewatcher.shadow_enabled.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setShadowEnabled)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.thewatcher.shadow_fear_threshold"), TheWatcherConfig.shadowFearThreshold())
                .setDefaultValue(100)
                .setMin(1).setMax(100)
                .setTooltip(Component.translatable("config.thewatcher.shadow_fear_threshold.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setShadowFearThreshold)
                .build());

        general.addEntry(entryBuilder.startStringDropdownMenu(Component.translatable("config.thewatcher.fear_bar_anchor"), TheWatcherConfig.fearBarAnchor())
                .setDefaultValue("RIGHT")
                .setSelections(java.util.List.of("LEFT", "RIGHT"))
                .setTooltip(Component.translatable("config.thewatcher.fear_bar_anchor.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setFearBarAnchor)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.thewatcher.fear_bar_y_offset"), TheWatcherConfig.fearBarYOffset())
                .setDefaultValue(0)
                .setTooltip(Component.translatable("config.thewatcher.fear_bar_y_offset.tooltip"))
                .setSaveConsumer(TheWatcherConfig::setFearBarYOffset)
                .build());

        return builder.build();
    }
}

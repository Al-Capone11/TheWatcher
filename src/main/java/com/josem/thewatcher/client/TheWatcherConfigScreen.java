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
                .setTooltip(Component.literal("Multiplicador de la velocidad a la que el miedo aumenta."))
                .setSaveConsumer(TheWatcherConfig::setFearIncreaseMultiplier)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Fear Decrease Multiplier"), TheWatcherConfig.fearDecreaseMultiplier())
                .setDefaultValue(1.0D)
                .setMin(0.0D).setMax(10.0D)
                .setTooltip(Component.literal("Multiplicador de la velocidad a la que el miedo disminuye."))
                .setSaveConsumer(TheWatcherConfig::setFearDecreaseMultiplier)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Torch Fear Slowdown"), TheWatcherConfig.torchFearReduction())
                .setDefaultValue(0.4D)
                .setMin(0.0D).setMax(1.0D)
                .setTooltip(Component.literal("Ralentiza el aumento de miedo al sostener antorchas. 0.4 = 40% más lento."))
                .setSaveConsumer(TheWatcherConfig::setTorchFearReduction)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Fake Crash Enabled"), TheWatcherConfig.fakeCrashEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Activa o desactiva el susto que simula un crasheo del juego (entre 40% y 80% de miedo)."))
                .setSaveConsumer(TheWatcherConfig::setFakeCrashEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Action Echoes Enabled"), TheWatcherConfig.actionEchoesEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Activa los ecos auditivos de acciones retrasadas (ej. escuchar que rompes un bloque segundos después)."))
                .setSaveConsumer(TheWatcherConfig::setActionEchoesEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Item Haunting Enabled"), TheWatcherConfig.itemHauntingEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Permite que los nombres de los ítems en tu inventario cambien temporalmente por susurros."))
                .setSaveConsumer(TheWatcherConfig::setItemHauntingEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Hotbar Drift Enabled"), TheWatcherConfig.hotbarDriftEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Activa el intercambio aleatorio de ítems en tu barra de herramientas cuando tienes mucho miedo."))
                .setSaveConsumer(TheWatcherConfig::setHotbarDriftEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Environmental Events Enabled"), TheWatcherConfig.environmentalEventsEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Activa eventos en el entorno como puertas que se cierran o antorchas que se rompen."))
                .setSaveConsumer(TheWatcherConfig::setEnvironmentalEventsEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Animal Staring Enabled"), TheWatcherConfig.animalStaringEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Los animales cercanos te mirarán fijamente cuando tu nivel de miedo sea alto."))
                .setSaveConsumer(TheWatcherConfig::setAnimalStaringEnabled)
                .build());

        general.addEntry(entryBuilder.startStringDropdownMenu(Component.literal("Fear Bar Anchor"), TheWatcherConfig.fearBarAnchor())
                .setDefaultValue("RIGHT")
                .setSelections(java.util.List.of("LEFT", "RIGHT"))
                .setTooltip(Component.literal("Lado de la pantalla donde se ancla la barra de miedo (LEFT o RIGHT)."))
                .setSaveConsumer(TheWatcherConfig::setFearBarAnchor)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.literal("Fear Bar Y Offset"), TheWatcherConfig.fearBarYOffset())
                .setDefaultValue(0)
                .setTooltip(Component.literal("Desplazamiento vertical de la barra de miedo (en píxeles). Valores negativos la suben, positivos la bajan."))
                .setSaveConsumer(TheWatcherConfig::setFearBarYOffset)
                .build());

        return builder.build();
    }
}

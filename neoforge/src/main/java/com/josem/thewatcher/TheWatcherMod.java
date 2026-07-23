package com.josem.thewatcher;

import com.josem.thewatcher.client.ModClientEvents;
import com.josem.thewatcher.entity.ModEntities;
import com.josem.thewatcher.network.ModNetwork;
import com.josem.thewatcher.game.FearEventsNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TheWatcherMod.MOD_ID)
public final class TheWatcherMod {
    public static final String MOD_ID = "thewatcher";

    public TheWatcherMod(IEventBus modBus) {
        ModEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(FearEventsNeoForge::onAttributes);
        ModNetwork.register(modBus);
        ModClientEvents.register(modBus);
    }
}


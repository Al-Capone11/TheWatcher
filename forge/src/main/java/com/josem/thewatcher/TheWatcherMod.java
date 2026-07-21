package com.josem.thewatcher;

import com.josem.thewatcher.client.ModClientEvents;
import com.josem.thewatcher.entity.ModEntities;

import com.josem.thewatcher.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TheWatcherMod.MOD_ID)
public final class TheWatcherMod {
    public static final String MOD_ID = "thewatcher";

    public TheWatcherMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModClientEvents.register(modBus));
        ModNetwork.register();
    }
}


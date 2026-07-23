package com.josem.thewatcher;

import com.josem.thewatcher.entity.ModEntities;
import com.josem.thewatcher.game.FearEventsFabric;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public final class TheWatcherMod implements ModInitializer {
    public static final String MOD_ID = "thewatcher";

    @Override
    public void onInitialize() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
            com.josem.thewatcher.network.ClientHorrorPacket.TYPE, 
            com.josem.thewatcher.network.ClientHorrorPacket.CODEC
        );
        ModEntities.register();
        FearEventsFabric.init();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}


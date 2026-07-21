package com.josem.thewatcher;

import com.josem.thewatcher.entity.ModEntities;
import com.josem.thewatcher.game.FearEventsFabric;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public final class TheWatcherMod implements ModInitializer {
    public static final String MOD_ID = "thewatcher";

    @Override
    public void onInitialize() {
        ModEntities.register();
        FearEventsFabric.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}


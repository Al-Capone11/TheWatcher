package com.josem.echoofthevoid;

import com.josem.echoofthevoid.entity.ModEntities;
import com.josem.echoofthevoid.game.FearSystem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public final class EchoOfTheVoidMod implements ModInitializer {
    public static final String MOD_ID = "echoofthevoid";

    @Override
    public void onInitialize() {
        ModEntities.register();
        FearSystem.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

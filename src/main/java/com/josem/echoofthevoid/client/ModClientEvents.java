package com.josem.echoofthevoid.client;

import com.josem.echoofthevoid.EchoOfTheVoidMod;
import com.josem.echoofthevoid.entity.ModEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ModClientEvents {
    public static final ModelLayerLocation SHADOW_LAYER =
        new ModelLayerLocation(new ResourceLocation(EchoOfTheVoidMod.MOD_ID, "shadow_stalker"), "main");

    private ModClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModClientEvents::registerLayerDefinitions);
        modBus.addListener(ModClientEvents::registerRenderers);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            SHADOW_LAYER,
            () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64)
        );
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHADOW_STALKER.get(), ShadowStalkerRenderer::new);
    }
}

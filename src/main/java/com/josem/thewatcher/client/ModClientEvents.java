package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import com.josem.thewatcher.entity.ModEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ModClientEvents {
    public static final ModelLayerLocation SHADOW_LAYER =
        new ModelLayerLocation(new ResourceLocation(TheWatcherMod.MOD_ID, "thewatcherentity"), "main");

    private ModClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModClientEvents::registerLayerDefinitions);
        modBus.addListener(ModClientEvents::registerRenderers);
        modBus.addListener(ModClientEvents::registerGuiOverlays);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            SHADOW_LAYER,
            () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64)
        );
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THE_WATCHER.get(), TheWatcherRenderer::new);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("fear_bar", FearBarOverlay.OVERLAY);
    }
}


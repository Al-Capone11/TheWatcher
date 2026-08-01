package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import com.josem.thewatcher.entity.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public final class ModClientEvents {
    public static final ModelLayerLocation SHADOW_LAYER =
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheWatcherMod.MOD_ID, "thewatcherentity"), "main");

    private ModClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModClientEvents::registerLayerDefinitions);
        modBus.addListener(ModClientEvents::registerRenderers);
        modBus.addListener(ModClientEvents::registerGuiLayers);

        // Client-side game bus events (registered on NeoForge.EVENT_BUS, not modBus)
        NeoForge.EVENT_BUS.addListener(ModClientEvents::onClientTick);

        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (mc, screen) -> TheWatcherConfigScreen.create(screen)
        );
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        ClientEffects.onClientTick();
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            SHADOW_LAYER,
            TheWatcherModel::createBodyLayer
        );
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THE_WATCHER.get(), TheWatcherRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            ResourceLocation.fromNamespaceAndPath(TheWatcherMod.MOD_ID, "fear_bar"),
            (guiGraphics, deltaTracker) -> FearBarOverlay.render(
                guiGraphics,
                Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight()
            )
        );
    }
}

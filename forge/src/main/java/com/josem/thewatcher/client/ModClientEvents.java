package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import com.josem.thewatcher.entity.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class ModClientEvents {
    public static final ModelLayerLocation SHADOW_LAYER =
        new ModelLayerLocation(new ResourceLocation(TheWatcherMod.MOD_ID, "thewatcherentity"), "main");

    private ModClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModClientEvents::registerLayerDefinitions);
        modBus.addListener(ModClientEvents::registerRenderers);
        modBus.addListener(ModClientEvents::registerGuiOverlays);

        // Client tick — registered on the Forge game event bus (not modBus)
        MinecraftForge.EVENT_BUS.addListener(ModClientEvents::onClientTick);

        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> TheWatcherConfigScreen.create(screen))
        );
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientEffects.onClientTick();
        }
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

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("fear_bar", new net.minecraftforge.client.gui.overlay.IGuiOverlay() {
            @Override
            public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTick, int width, int height) {
                FearBarOverlay.render(guiGraphics, width, height);
            }
        });
    }
}

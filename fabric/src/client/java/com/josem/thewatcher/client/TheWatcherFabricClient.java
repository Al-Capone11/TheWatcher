package com.josem.thewatcher.client;

import com.josem.thewatcher.entity.ModEntities;
import com.josem.thewatcher.network.ModNetworkIds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class TheWatcherFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ModClientEvents.SHADOW_LAYER, ModClientEvents::createShadowLayer);
        EntityRendererRegistry.register(ModEntities.THE_WATCHER, TheWatcherRenderer::new);
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            FearBarOverlay.render(graphics, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEffects.onClientTick());
        ClientPlayNetworking.registerGlobalReceiver(
            com.josem.thewatcher.network.ClientHorrorPacket.TYPE,
            (payload, context) -> {
                int type = payload.eventId();
                int value = payload.value();
                context.client().execute(() -> {
                    switch (type) {
                        case 1  -> ClientEffects.playFootstep();
                        case 2  -> ClientEffects.playFalseCreeper();
                        case 3  -> ClientEffects.playWhisper();
                        case 4  -> { if (com.josem.thewatcher.game.TheWatcherConfig.fakeCrashEnabled()) ClientEffects.showFakeCrash(); }
                        case 5  -> ClientEffects.playEchoBreak();
                        case 6  -> ClientEffects.playEchoChest();
                        case 7  -> ClientEffects.playClicker();
                        case 8  -> ClientEffects.playGrowl();
                        case 100 -> ClientEffects.setFearLevel(value);
                        case 101 -> ClientEffects.setFearBarEnabled(value != 0);
                        default -> {}
                    }
                });
            }
        );
    }
}



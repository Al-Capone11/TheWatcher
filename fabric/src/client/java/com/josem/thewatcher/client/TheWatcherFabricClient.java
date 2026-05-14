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
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> FearBarOverlay.render(graphics));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEffects.tickFakeCrash(client));
        ClientPlayNetworking.registerGlobalReceiver(ModNetworkIds.MAIN, ClientHorrorPacket::handle);
    }
}



package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class ModClientEvents {
    public static final ModelLayerLocation SHADOW_LAYER = new ModelLayerLocation(TheWatcherMod.id("thewatcherentity"), "main");

    private ModClientEvents() {
    }

    public static LayerDefinition createShadowLayer() {
        return TheWatcherModel.createBodyLayer();
    }
}


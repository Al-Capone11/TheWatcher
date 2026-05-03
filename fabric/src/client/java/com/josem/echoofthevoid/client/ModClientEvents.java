package com.josem.echoofthevoid.client;

import com.josem.echoofthevoid.EchoOfTheVoidMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class ModClientEvents {
    public static final ModelLayerLocation SHADOW_LAYER = new ModelLayerLocation(EchoOfTheVoidMod.id("shadow_stalker"), "main");

    private ModClientEvents() {
    }

    public static LayerDefinition createShadowLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    }
}

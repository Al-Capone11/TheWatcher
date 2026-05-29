package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import com.josem.thewatcher.entity.TheWatcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TheWatcherRenderer extends LivingEntityRenderer<TheWatcherEntity, TheWatcherModel<TheWatcherEntity>> {
    private static final ResourceLocation SKIN = TheWatcherMod.id("textures/entity/thewatcher.png");

    public TheWatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new TheWatcherModel<>(context.bakeLayer(ModClientEvents.SHADOW_LAYER)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(TheWatcherEntity entity) {
        return SKIN;
    }

    @Override
    protected boolean shouldShowName(TheWatcherEntity entity) {
        return false;
    }
}


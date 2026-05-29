package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import com.josem.thewatcher.entity.TheWatcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TheWatcherRenderer extends LivingEntityRenderer<TheWatcherEntity, TheWatcherModel<TheWatcherEntity>> {
    private static final ResourceLocation SKIN = new ResourceLocation(TheWatcherMod.MOD_ID, "textures/entity/thewatcher.png");

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

    @Override
    protected void setupRotations(TheWatcherEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }
}


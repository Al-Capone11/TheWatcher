package com.josem.thewatcher.client;

import com.josem.thewatcher.TheWatcherMod;
import com.josem.thewatcher.entity.TheWatcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

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

    @Override
    protected void setupRotations(TheWatcherEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);
            return;
        }

        double dx = player.getX() - entity.getX();
        double dz = player.getZ() - entity.getZ();
        float yawToPlayer = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yawToPlayer));
    }
}

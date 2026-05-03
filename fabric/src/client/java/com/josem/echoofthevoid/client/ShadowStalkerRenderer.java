package com.josem.echoofthevoid.client;

import com.josem.echoofthevoid.entity.ShadowStalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ShadowStalkerRenderer extends LivingEntityRenderer<ShadowStalkerEntity, PlayerModel<ShadowStalkerEntity>> {
    private static final ResourceLocation SKIN = new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public ShadowStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModClientEvents.SHADOW_LAYER), false), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowStalkerEntity entity) {
        return SKIN;
    }

    @Override
    public void render(ShadowStalkerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.1F, 1.1F, 1.1F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        this.model.attackTime = 0.0F;
        this.model.crouching = false;
        this.model.riding = false;
        this.model.young = false;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, getOverlayCoords(entity, 0.0F), 0.0F, 0.0F, 0.0F, 0.92F);
        poseStack.popPose();
    }
}

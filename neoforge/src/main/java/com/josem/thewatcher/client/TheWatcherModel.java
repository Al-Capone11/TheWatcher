package com.josem.thewatcher.client;

import com.josem.thewatcher.entity.TheWatcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class TheWatcherModel<T extends TheWatcherEntity> extends EntityModel<T> {
    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;
    private final ModelPart root;

    public TheWatcherModel(ModelPart root) {
        this.root = root.getChild("watcher");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition watcher = root.addOrReplaceChild("watcher", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 0.0F));

        PartDefinition head = watcher.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -30.0F, 0.0F));
        head.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(18, 25).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 1.0F, 5.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);

        PartDefinition torso = watcher.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, 0.0F));
        torso.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 15).addBox(-6.0F, -12.0F, -3.0F, 12.0F, 5.0F, 5.0F), PartPose.ZERO);
        torso.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, -7.0F, -2.5F, 6.0F, 7.0F, 4.0F), PartPose.ZERO);

        PartDefinition leftArm = watcher.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(7.0F, -30.0F, 0.0F));
        leftArm.addOrReplaceChild("left_upper_arm", CubeListBuilder.create().texOffs(18, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F), PartPose.ZERO);
        PartDefinition leftForearm = watcher.addOrReplaceChild("left_forearm", CubeListBuilder.create(), PartPose.offset(7.0F, -21.0F, 0.0F));
        leftForearm.addOrReplaceChild("left_lower_arm", CubeListBuilder.create().texOffs(34, 31).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.ZERO);

        PartDefinition rightArm = watcher.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-7.0F, -30.0F, 0.0F));
        rightArm.addOrReplaceChild("right_upper_arm", CubeListBuilder.create().texOffs(26, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F), PartPose.ZERO);
        PartDefinition rightForearm = watcher.addOrReplaceChild("right_forearm", CubeListBuilder.create(), PartPose.offset(-7.0F, -21.0F, 0.0F));
        rightForearm.addOrReplaceChild("right_lower_arm", CubeListBuilder.create().texOffs(0, 35).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.ZERO);

        PartDefinition leftThigh = watcher.addOrReplaceChild("left_thigh", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -18.0F, -1.0F, -25.0F * DEG_TO_RAD, 0.0F, 0.0F));
        leftThigh.addOrReplaceChild("left_upper_leg", CubeListBuilder.create().texOffs(31, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 10.0F, 3.0F), PartPose.ZERO);
        PartDefinition leftCalf = watcher.addOrReplaceChild("left_calf", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -9.0F, 0.0F, 50.0F * DEG_TO_RAD, 0.0F, 0.0F));
        leftCalf.addOrReplaceChild("left_lower_leg", CubeListBuilder.create().texOffs(4, 35).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 10.0F, 1.0F), PartPose.ZERO);

        PartDefinition rightThigh = watcher.addOrReplaceChild("right_thigh", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -18.0F, -1.0F, -25.0F * DEG_TO_RAD, 0.0F, 0.0F));
        rightThigh.addOrReplaceChild("right_upper_leg", CubeListBuilder.create().texOffs(31, 10).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 10.0F, 3.0F), PartPose.ZERO);
        PartDefinition rightCalf = watcher.addOrReplaceChild("right_calf", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -9.0F, 0.0F, 50.0F * DEG_TO_RAD, 0.0F, 0.0F));
        rightCalf.addOrReplaceChild("right_lower_leg", CubeListBuilder.create().texOffs(8, 35).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 10.0F, 1.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

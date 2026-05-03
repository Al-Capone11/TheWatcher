package com.josem.echoofthevoid.entity;

import com.josem.echoofthevoid.EchoOfTheVoidMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<ShadowStalkerEntity> SHADOW_STALKER = EntityType.Builder
        .<ShadowStalkerEntity>of(ShadowStalkerEntity::new, MobCategory.MONSTER)
        .sized(0.66F, 2.2F)
        .clientTrackingRange(32)
        .updateInterval(1)
        .build(EchoOfTheVoidMod.MOD_ID + ":shadow_stalker");

    private ModEntities() {
    }

    public static void register() {
        net.minecraft.core.Registry.register(BuiltInRegistries.ENTITY_TYPE, EchoOfTheVoidMod.id("shadow_stalker"), SHADOW_STALKER);
        FabricDefaultAttributeRegistry.register(SHADOW_STALKER, ShadowStalkerEntity.createAttributes());
    }
}

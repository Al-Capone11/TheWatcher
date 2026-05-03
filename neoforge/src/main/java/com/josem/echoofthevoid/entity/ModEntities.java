package com.josem.echoofthevoid.entity;

import com.josem.echoofthevoid.EchoOfTheVoidMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EchoOfTheVoidMod.MOD_ID);

    public static final RegistryObject<EntityType<ShadowStalkerEntity>> SHADOW_STALKER = ENTITY_TYPES.register(
        "shadow_stalker",
        () -> EntityType.Builder.<ShadowStalkerEntity>of(ShadowStalkerEntity::new, MobCategory.MONSTER)
            .sized(0.66F, 2.2F)
            .clientTrackingRange(32)
            .updateInterval(1)
            .build("shadow_stalker")
    );

    private ModEntities() {
    }
}

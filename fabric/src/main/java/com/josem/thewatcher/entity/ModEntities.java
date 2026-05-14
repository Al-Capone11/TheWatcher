package com.josem.thewatcher.entity;

import com.josem.thewatcher.TheWatcherMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<TheWatcherEntity> THE_WATCHER = EntityType.Builder
        .<TheWatcherEntity>of(TheWatcherEntity::new, MobCategory.MONSTER)
        .sized(0.66F, 2.2F)
        .clientTrackingRange(32)
        .updateInterval(1)
        .build(TheWatcherMod.MOD_ID + ":thewatcherentity");

    private ModEntities() {
    }

    public static void register() {
        net.minecraft.core.Registry.register(BuiltInRegistries.ENTITY_TYPE, TheWatcherMod.id("thewatcherentity"), THE_WATCHER);
        FabricDefaultAttributeRegistry.register(THE_WATCHER, TheWatcherEntity.createAttributes());
    }
}


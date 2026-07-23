package com.josem.thewatcher.entity;

import com.josem.thewatcher.TheWatcherMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, TheWatcherMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TheWatcherEntity>> THE_WATCHER =
        ENTITY_TYPES.register(
            "thewatcherentity",
            () -> EntityType.Builder.<TheWatcherEntity>of(TheWatcherEntity::new, MobCategory.MONSTER)
                .sized(0.66F, 2.2F)
                .clientTrackingRange(32)
                .updateInterval(1)
                .build("thewatcherentity")
        );

    private ModEntities() {
    }
}

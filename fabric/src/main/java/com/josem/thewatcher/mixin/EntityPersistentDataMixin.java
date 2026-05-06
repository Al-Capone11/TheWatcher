package com.josem.thewatcher.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityPersistentDataMixin implements EntityPersistentDataHolder {
    @Unique
    private final CompoundTag thewatcher$persistentData = new CompoundTag();

    @Override
    public CompoundTag thewatcher$getPersistentData() {
        return thewatcher$persistentData;
    }
}


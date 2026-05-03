package com.josem.echoofthevoid.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityPersistentDataMixin implements EntityPersistentDataHolder {
    @Unique
    private final CompoundTag echoofthevoid$persistentData = new CompoundTag();

    @Override
    public CompoundTag echoofthevoid$getPersistentData() {
        return echoofthevoid$persistentData;
    }
}

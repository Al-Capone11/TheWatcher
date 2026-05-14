package com.josem.thewatcher.mixin;

import com.josem.thewatcher.bridge.EntityPersistentDataHolder;
import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPersistentDataMixin implements EntityPersistentDataHolder {
    @Unique
    private static final String THE_WATCHER_DATA = "TheWatcherData";

    @Unique
    private final CompoundTag thewatcher$persistentData = new CompoundTag();

    @Override
    public CompoundTag thewatcher$getPersistentData() {
        return thewatcher$persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void thewatcher$savePersistentData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!thewatcher$persistentData.isEmpty()) {
            cir.getReturnValue().put(THE_WATCHER_DATA, thewatcher$persistentData.copy());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void thewatcher$loadPersistentData(CompoundTag tag, CallbackInfo ci) {
        for (String key : new ArrayList<>(thewatcher$persistentData.getAllKeys())) {
            thewatcher$persistentData.remove(key);
        }

        if (tag.contains(THE_WATCHER_DATA, 10)) {
            CompoundTag savedData = tag.getCompound(THE_WATCHER_DATA);
            for (String key : savedData.getAllKeys()) {
                thewatcher$persistentData.put(key, savedData.get(key).copy());
            }
        }
    }
}

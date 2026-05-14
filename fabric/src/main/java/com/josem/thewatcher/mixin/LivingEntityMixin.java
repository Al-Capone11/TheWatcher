package com.josem.thewatcher.mixin;

import com.josem.thewatcher.game.FearSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private ItemStack thewatcher$usedItem = ItemStack.EMPTY;

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void thewatcher$captureUsedItem(CallbackInfo ci) {
        thewatcher$usedItem = ((LivingEntity) (Object) this).getUseItem().copy();
    }

    @Inject(method = "completeUsingItem", at = @At("TAIL"))
    private void thewatcher$afterCompleteUsingItem(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            FearSystem.onComfortFoodConsumed(player, thewatcher$usedItem);
        }
        thewatcher$usedItem = ItemStack.EMPTY;
    }
}

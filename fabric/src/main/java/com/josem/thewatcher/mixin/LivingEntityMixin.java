package com.josem.thewatcher.mixin;

import com.josem.thewatcher.game.FearSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "completeUsingItem", at = @At("TAIL"))
    private void thewatcher$afterCompleteUsingItem(CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this instanceof ServerPlayer player) {
            FearSystem.onComfortFoodConsumed(player);
        }
    }
}


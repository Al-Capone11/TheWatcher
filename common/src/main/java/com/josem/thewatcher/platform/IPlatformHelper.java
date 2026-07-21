package com.josem.thewatcher.platform;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public interface IPlatformHelper {
    CompoundTag getPersistentData(Entity entity);
    void sendClientEvent(ServerPlayer player, int eventId);
    void sendFearState(ServerPlayer player, int fear, boolean visible);

    boolean hasStareGoal(net.minecraft.world.entity.animal.Animal animal);
    void addStareGoal(net.minecraft.world.entity.animal.Animal animal, net.minecraft.world.entity.player.Player player);
    
    com.josem.thewatcher.entity.TheWatcherEntity createWatcherEntity(net.minecraft.world.level.Level level);
}

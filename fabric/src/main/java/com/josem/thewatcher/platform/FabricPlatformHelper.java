package com.josem.thewatcher.platform;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import com.josem.thewatcher.bridge.EntityPersistentDataHolder;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public CompoundTag getPersistentData(Entity entity) {
        return ((EntityPersistentDataHolder) entity).thewatcher$getPersistentData();
    }

    @Override
    public void sendClientEvent(ServerPlayer player, int eventId) {
        ServerPlayNetworking.send(player, new com.josem.thewatcher.network.ClientHorrorPacket(eventId));
    }

    @Override
    public void sendFearState(ServerPlayer player, int fear, boolean visible) {
        ServerPlayNetworking.send(player, new com.josem.thewatcher.network.ClientHorrorPacket(100, fear));
        ServerPlayNetworking.send(player, new com.josem.thewatcher.network.ClientHorrorPacket(101, visible ? 1 : 0));
    }

    @Override
    public boolean hasStareGoal(net.minecraft.world.entity.animal.Animal animal) {
        return ((com.josem.thewatcher.mixin.MobAccessor) animal).thewatcher$getGoalSelector().getAvailableGoals().stream()
            .anyMatch(g -> g.getGoal() instanceof com.josem.thewatcher.entity.StareAtPlayerGoal);
    }

    @Override
    public void addStareGoal(net.minecraft.world.entity.animal.Animal animal, net.minecraft.world.entity.player.Player player) {
        ((com.josem.thewatcher.mixin.MobAccessor) animal).thewatcher$getGoalSelector().addGoal(0, new com.josem.thewatcher.entity.StareAtPlayerGoal(animal, player));
    }

    @Override
    public com.josem.thewatcher.entity.TheWatcherEntity createWatcherEntity(net.minecraft.world.level.Level level) {
        return com.josem.thewatcher.entity.ModEntities.THE_WATCHER.create(level);
    }
}

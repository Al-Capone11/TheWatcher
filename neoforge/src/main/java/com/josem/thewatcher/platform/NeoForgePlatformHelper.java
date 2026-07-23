package com.josem.thewatcher.platform;

import com.josem.thewatcher.network.ClientHorrorPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public CompoundTag getPersistentData(Entity entity) {
        return entity.getPersistentData();
    }
    
    @Override
    public void sendClientEvent(ServerPlayer player, int eventId) {
        PacketDistributor.sendToPlayer(player, new ClientHorrorPacket(eventId));
    }

    @Override
    public void sendFearState(ServerPlayer player, int fear, boolean visible) {
        PacketDistributor.sendToPlayer(player, new ClientHorrorPacket(100, fear));
        PacketDistributor.sendToPlayer(player, new ClientHorrorPacket(101, visible ? 1 : 0));
    }

    @Override
    public boolean hasStareGoal(net.minecraft.world.entity.animal.Animal animal) {
        return animal.goalSelector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof com.josem.thewatcher.entity.StareAtPlayerGoal);
    }

    @Override
    public void addStareGoal(net.minecraft.world.entity.animal.Animal animal, net.minecraft.world.entity.player.Player player) {
        animal.goalSelector.addGoal(0, new com.josem.thewatcher.entity.StareAtPlayerGoal(animal, player));
    }

    @Override
    public com.josem.thewatcher.entity.TheWatcherEntity createWatcherEntity(net.minecraft.world.level.Level level) {
        return com.josem.thewatcher.entity.ModEntities.THE_WATCHER.get().create(level);
    }
}

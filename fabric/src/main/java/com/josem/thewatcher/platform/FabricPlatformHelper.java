package com.josem.thewatcher.platform;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import io.netty.buffer.Unpooled;
import com.josem.thewatcher.bridge.EntityPersistentDataHolder;
import com.josem.thewatcher.network.ModNetworkIds;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public CompoundTag getPersistentData(Entity entity) {
        return ((EntityPersistentDataHolder) entity).thewatcher$getPersistentData();
    }

    @Override
    public void sendClientEvent(ServerPlayer player, int eventId) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(eventId);
        buf.writeVarInt(0);
        ServerPlayNetworking.send(player, ModNetworkIds.MAIN, buf);
    }

    @Override
    public void sendFearState(ServerPlayer player, int fear, boolean visible) {
        FriendlyByteBuf bufFear = new FriendlyByteBuf(Unpooled.buffer());
        bufFear.writeVarInt(100);
        bufFear.writeVarInt(fear);
        ServerPlayNetworking.send(player, ModNetworkIds.MAIN, bufFear);

        FriendlyByteBuf bufVis = new FriendlyByteBuf(Unpooled.buffer());
        bufVis.writeVarInt(101);
        bufVis.writeVarInt(visible ? 1 : 0);
        ServerPlayNetworking.send(player, ModNetworkIds.MAIN, bufVis);
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

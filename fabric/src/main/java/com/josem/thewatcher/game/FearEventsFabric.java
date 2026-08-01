package com.josem.thewatcher.game;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FearEventsFabric {
    // Tracks the item a player was using last tick so we can detect when they finish
    private static final Map<UUID, ItemStack> usingItemLastTick = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CommonFearSystem.onPlayerTick(player);

                // Detect when a player finishes using/eating an item
                UUID id = player.getUUID();
                boolean isUsing = player.isUsingItem();
                ItemStack currentItem = player.getUseItem();

                if (usingItemLastTick.containsKey(id) && !isUsing) {
                    // Player was using an item last tick and stopped — they finished consuming it
                    ItemStack finishedItem = usingItemLastTick.get(id);
                    if (!finishedItem.isEmpty()) {
                        CommonFearSystem.onItemUseFinish(player, finishedItem);
                    }
                    usingItemLastTick.remove(id);
                } else if (isUsing) {
                    usingItemLastTick.put(id, currentItem.copy());
                } else {
                    usingItemLastTick.remove(id);
                }
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                CommonFearSystem.onUseBlock(serverPlayer, world.getBlockState(hitResult.getBlockPos()), hitResult.getBlockPos());
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                CommonFearSystem.onBlockBreak(serverPlayer);
            }
            return true;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommonFearSystem.onCommandsRegister(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CommonFearSystem.onPlayerLogin(handler.getPlayer());
        });

        // Clean up tracker when a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            usingItemLastTick.remove(handler.getPlayer().getUUID());
        });
    }
}

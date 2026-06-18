package com.josem.thewatcher.client;

import com.josem.thewatcher.game.TheWatcherConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public final class ClientHorrorPacket {
    private ClientHorrorPacket() {}

    public static void handle(Minecraft client, ClientPacketListener handler,
                              FriendlyByteBuf buffer, PacketSender responseSender) {
        int type  = buffer.readVarInt();
        int value = buffer.readVarInt();
        client.execute(() -> {
            switch (type) {
                case 1  -> ClientEffects.playFootstep();
                case 2  -> ClientEffects.playFalseCreeper();
                case 3  -> ClientEffects.playWhisper();
                case 4  -> { if (TheWatcherConfig.fakeCrashEnabled()) ClientEffects.showFakeCrash(); }
                case 5  -> ClientEffects.playEchoBreak();  // new: eco de minería
                case 6  -> ClientEffects.playEchoChest();  // new: eco de cofre
                case 7  -> ClientEffects.playClicker();
                case 8  -> ClientEffects.playGrowl();
                case 100 -> ClientEffects.setFearLevel(value);
                case 101 -> ClientEffects.setFearBarEnabled(value != 0);
                default -> {}
            }
        });
    }
}

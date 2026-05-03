package com.josem.echoofthevoid.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public final class ClientHorrorPacket {
    private ClientHorrorPacket() {
    }

    public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
        int type = buffer.readVarInt();
        int value = buffer.readVarInt();
        client.execute(() -> {
            switch (type) {
                case 1 -> ClientEffects.playFootstep();
                case 2 -> ClientEffects.playFalseCreeper();
                case 3 -> ClientEffects.playWhisper();
                case 4 -> ClientEffects.showFakeCrash();
                case 100 -> ClientEffects.setFearLevel(value);
                case 101 -> ClientEffects.setFearBarEnabled(value != 0);
                default -> {
                }
            }
        });
    }
}

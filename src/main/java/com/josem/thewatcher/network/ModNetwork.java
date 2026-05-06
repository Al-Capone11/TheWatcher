package com.josem.thewatcher.network;

import com.josem.thewatcher.TheWatcherMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(TheWatcherMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
            packetId++,
            ClientHorrorPacket.class,
            ClientHorrorPacket::encode,
            ClientHorrorPacket::decode,
            ClientHorrorPacket::handle
        );
    }
}


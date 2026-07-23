package com.josem.thewatcher.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientHorrorPacket(int eventId, int value) implements CustomPacketPayload {
    public static final Type<ClientHorrorPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("thewatcher", "horror"));

    public static final StreamCodec<ByteBuf, ClientHorrorPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ClientHorrorPacket::eventId,
        ByteBufCodecs.VAR_INT, ClientHorrorPacket::value,
        ClientHorrorPacket::new
    );

    public ClientHorrorPacket(int eventId) {
        this(eventId, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

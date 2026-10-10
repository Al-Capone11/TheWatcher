package com.josem.thewatcher.network;

import com.josem.thewatcher.client.ClientPacketHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public final class ClientHorrorPacket {
    private final int type;
    private final int value;

    public ClientHorrorPacket(int type)            { this(type, 0); }
    public ClientHorrorPacket(int type, int value) { this.type = type; this.value = value; }

    public static void encode(ClientHorrorPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.type);
        buffer.writeVarInt(packet.value);
    }

    public static ClientHorrorPacket decode(FriendlyByteBuf buffer) {
        return new ClientHorrorPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(ClientHorrorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientPacketHandler.handle(packet.type, packet.value);
        });
        context.setPacketHandled(true);
    }
}

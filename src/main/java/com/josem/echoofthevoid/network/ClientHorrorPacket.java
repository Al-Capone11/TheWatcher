package com.josem.echoofthevoid.network;

import com.josem.echoofthevoid.client.ClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public final class ClientHorrorPacket {
    private final int eventId;

    public ClientHorrorPacket(int eventId) {
        this.eventId = eventId;
    }

    public static void encode(ClientHorrorPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.eventId);
    }

    public static ClientHorrorPacket decode(FriendlyByteBuf buffer) {
        return new ClientHorrorPacket(buffer.readVarInt());
    }

    public static void handle(ClientHorrorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            switch (packet.eventId) {
                case 1 -> ClientEffects.playFootstep();
                case 2 -> ClientEffects.playFalseCreeper();
                case 3 -> ClientEffects.playWhisper();
                case 4 -> ClientEffects.showFakeCrash();
                default -> {
                }
            }
        });
        context.setPacketHandled(true);
    }
}

package com.josem.echoofthevoid.network;

import com.josem.echoofthevoid.client.ClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public final class ClientHorrorPacket {
    private final int type;
    private final int value;

    public ClientHorrorPacket(int type) {
        this(type, 0);
    }

    public ClientHorrorPacket(int type, int value) {
        this.type = type;
        this.value = value;
    }

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
            switch (packet.type) {
                case 1 -> ClientEffects.playFootstep();
                case 2 -> ClientEffects.playFalseCreeper();
                case 3 -> ClientEffects.playWhisper();
                case 4 -> ClientEffects.showFakeCrash();
                case 100 -> ClientEffects.setFearLevel(packet.value);
                case 101 -> ClientEffects.setFearBarEnabled(packet.value != 0);
                default -> {
                }
            }
        });
        context.setPacketHandled(true);
    }
}

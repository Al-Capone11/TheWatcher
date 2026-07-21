package com.josem.thewatcher.network;

import com.josem.thewatcher.client.ClientEffects;
import com.josem.thewatcher.game.TheWatcherConfig;
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
            switch (packet.type) {
                case 1  -> ClientEffects.playFootstep();
                case 2  -> ClientEffects.playFalseCreeper();
                case 3  -> ClientEffects.playWhisper();
                case 4  -> { if (TheWatcherConfig.fakeCrashEnabled()) ClientEffects.showFakeCrash(); }
                case 5  -> ClientEffects.playEchoBreak();  // new: eco de minería
                case 6  -> ClientEffects.playEchoChest();  // new: eco de cofre
                case 7  -> ClientEffects.playClicker();
                case 8  -> ClientEffects.playGrowl();
                case 100 -> ClientEffects.setFearLevel(packet.value);
                case 101 -> ClientEffects.setFearBarEnabled(packet.value != 0);
                default -> {}
            }
        });
        context.setPacketHandled(true);
    }
}

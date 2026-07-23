package com.josem.thewatcher.network;

import com.josem.thewatcher.client.ClientEffects;
import com.josem.thewatcher.game.TheWatcherConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModNetwork::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            ClientHorrorPacket.TYPE,
            ClientHorrorPacket.CODEC,
            (packet, context) -> context.enqueueWork(() -> handlePacket(packet))
        );
    }

    private static void handlePacket(ClientHorrorPacket packet) {
        switch (packet.eventId()) {
            case 1  -> ClientEffects.playFootstep();
            case 2  -> ClientEffects.playFalseCreeper();
            case 3  -> ClientEffects.playWhisper();
            case 4  -> { if (TheWatcherConfig.fakeCrashEnabled()) ClientEffects.showFakeCrash(); }
            case 5  -> ClientEffects.playEchoBreak();
            case 6  -> ClientEffects.playEchoChest();
            case 7  -> ClientEffects.playClicker();
            case 8  -> ClientEffects.playGrowl();
            case 100 -> ClientEffects.setFearLevel(packet.value());
            case 101 -> ClientEffects.setFearBarEnabled(packet.value() != 0);
            default -> {}
        }
    }
}


package com.josem.thewatcher.network;

import com.josem.thewatcher.client.ClientPacketHandler;
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
            (packet, context) -> context.enqueueWork(
                () -> ClientPacketHandler.handle(packet.eventId(), packet.value())
            )
        );
    }
}


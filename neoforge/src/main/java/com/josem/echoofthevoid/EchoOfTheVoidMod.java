package com.josem.echoofthevoid;

import com.josem.echoofthevoid.client.ModClientEvents;
import com.josem.echoofthevoid.entity.ModEntities;
import com.josem.echoofthevoid.game.FearSystem;
import com.josem.echoofthevoid.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EchoOfTheVoidMod.MOD_ID)
public final class EchoOfTheVoidMod {
    public static final String MOD_ID = "echoofthevoid";

    public EchoOfTheVoidMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(FearSystem::onAttributes);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModClientEvents.register(modBus));
        ModNetwork.register();
    }
}

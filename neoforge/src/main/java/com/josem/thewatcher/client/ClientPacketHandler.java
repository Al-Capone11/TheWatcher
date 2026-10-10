package com.josem.thewatcher.client;

import com.josem.thewatcher.game.TheWatcherConfig;

public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void handle(int eventId, int value) {
        switch (eventId) {
            case 1   -> ClientEffects.playFootstep();
            case 2   -> ClientEffects.playFalseCreeper();
            case 3   -> ClientEffects.playWhisper();
            case 4   -> { if (TheWatcherConfig.fakeCrashEnabled()) ClientEffects.showFakeCrash(); }
            case 5   -> ClientEffects.playEchoBreak();
            case 6   -> ClientEffects.playEchoChest();
            case 7   -> ClientEffects.playClicker();
            case 8   -> ClientEffects.playGrowl();
            case 100 -> ClientEffects.setFearLevel(value);
            case 101 -> ClientEffects.setFearBarEnabled(value != 0);
            default  -> {}
        }
    }
}

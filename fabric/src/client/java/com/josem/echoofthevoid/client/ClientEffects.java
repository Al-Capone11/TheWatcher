package com.josem.echoofthevoid.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ClientEffects {
    private static int fakeCrashTicks;
    private static int fearLevel;
    private static boolean fearBarEnabled = true;

    private ClientEffects() {
    }

    public static void playFootstep() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.STONE_STEP, SoundSource.AMBIENT, 0.6F, 0.8F, false);
    }

    public static void playFalseCreeper() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Vec3 back = player.getLookAngle().scale(-3.0D);
        player.level().playLocalSound(player.getX() + back.x, player.getY(), player.getZ() + back.z, SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.2F, 1.0F, false);
    }

    public static void playWhisper() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        RandomSource random = player.getRandom();
        player.level().playLocalSound(
            player.getX() + random.nextInt(7) - 3,
            player.getY() + 1.0D,
            player.getZ() + random.nextInt(7) - 3,
            SoundEvents.AMBIENT_CAVE.value(),
            SoundSource.AMBIENT,
            0.35F,
            0.6F + random.nextFloat() * 0.3F,
            false
        );
    }

    public static void showFakeCrash() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        fakeCrashTicks = 40;
        minecraft.setScreen(new FakeDisconnectScreen());
    }

    public static void tickFakeCrash(Minecraft minecraft) {
        if (fakeCrashTicks <= 0) {
            return;
        }

        fakeCrashTicks--;
        if (fakeCrashTicks <= 0 && minecraft.screen instanceof FakeDisconnectScreen) {
            minecraft.setScreen(null);
        }
    }

    public static int getFearLevel() {
        return fearLevel;
    }

    public static boolean isFearBarEnabled() {
        return fearBarEnabled;
    }

    public static void setFearLevel(int value) {
        fearLevel = value;
    }

    public static void setFearBarEnabled(boolean enabled) {
        fearBarEnabled = enabled;
    }
}

package com.josem.thewatcher.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
public final class ClientEffects {
    private static int fakeCrashTicks;
    private static int fearLevel;
    private static boolean fearBarEnabled = true;

    private ClientEffects() {}

    // ── existing sounds ───────────────────────────────────────────────────────

    public static void playFootstep() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        player.level().playLocalSound(
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.STONE_STEP, SoundSource.AMBIENT,
            0.6F, 0.8F, false);
    }

    public static void playFalseCreeper() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Vec3 back = player.getLookAngle().scale(-3.0D);
        player.level().playLocalSound(
            player.getX() + back.x, player.getY(), player.getZ() + back.z,
            SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE,
            0.2F, 1.0F, false);
    }

    public static void playWhisper() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        RandomSource random = player.getRandom();
        player.level().playLocalSound(
            player.getX() + random.nextInt(7) - 3,
            player.getY() + 1.0D,
            player.getZ() + random.nextInt(7) - 3,
            SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
            0.35F, 0.6F + random.nextFloat() * 0.3F, false);
    }

    public static void showFakeCrash() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        fakeCrashTicks = 40;
        minecraft.setScreen(new FakeDisconnectScreen());
    }

    // ── new echo sounds (0.1.8) ───────────────────────────────────────────────

    /** Plays a muffled echo of a block being mined from a random nearby direction. */
    public static void playEchoBreak() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        RandomSource random = player.getRandom();
        player.level().playLocalSound(
            player.getX() + random.nextInt(17) - 8,
            player.getY(),
            player.getZ() + random.nextInt(17) - 8,
            SoundEvents.STONE_STEP, SoundSource.AMBIENT,
            0.4F, 0.45F + random.nextFloat() * 0.15F, false);
    }

    /** Plays a faint echo of a chest opening from a random nearby direction. */
    public static void playEchoChest() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        RandomSource random = player.getRandom();
        player.level().playLocalSound(
            player.getX() + random.nextInt(17) - 8,
            player.getY(),
            player.getZ() + random.nextInt(17) - 8,
            SoundEvents.CHEST_OPEN, SoundSource.AMBIENT,
            0.3F, 0.85F + random.nextFloat() * 0.2F, false);
    }

    public static void playClicker() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        RandomSource random = player.getRandom();
        player.level().playLocalSound(
            player.getX() + random.nextInt(9) - 4,
            player.getY(),
            player.getZ() + random.nextInt(9) - 4,
            SoundEvents.WARDEN_TENDRIL_CLICKS, SoundSource.HOSTILE,
            1.0F, 0.4F + random.nextFloat() * 0.2F, false);
    }

    public static void playGrowl() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        RandomSource random = player.getRandom();
        player.level().playLocalSound(
            player.getX() + random.nextInt(9) - 4,
            player.getY(),
            player.getZ() + random.nextInt(9) - 4,
            SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE,
            0.8F, 0.3F + random.nextFloat() * 0.2F, false);
    }

    // ── fear bar state ────────────────────────────────────────────────────────

    public static int getFearLevel()               { return fearLevel; }
    public static boolean isFearBarEnabled()       { return fearBarEnabled; }
    public static void setFearLevel(int value)     { fearLevel = value; }
    public static void setFearBarEnabled(boolean b){ fearBarEnabled = b; }

    // ── tick ──────────────────────────────────────────────────────────────────

    public static void onClientTick() {
        if (fakeCrashTicks <= 0) return;
        fakeCrashTicks--;
        if (fakeCrashTicks > 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (screen instanceof FakeDisconnectScreen) minecraft.setScreen(null);
    }
}

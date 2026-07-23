package com.josem.thewatcher.client;


import com.josem.thewatcher.game.TheWatcherConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
public final class FearBarOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("thewatcher", "fear_bar");

    private FearBarOverlay() {}

    public static void render(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || !ClientEffects.isFearBarEnabled()) {
            return;
        }

        int fear = ClientEffects.getFearLevel();
        int frameWidth = 12;
        int frameHeight = 84;
        int innerHeight = 80;
        int filled = Math.max(0, Math.min(innerHeight, Math.round(innerHeight * (fear / 100.0F))));
        
        String anchor = TheWatcherConfig.fearBarAnchor();
        int x = "LEFT".equalsIgnoreCase(anchor) ? 12 : width - 24;
        int y = height / 2 - frameHeight / 2 + TheWatcherConfig.fearBarYOffset();

        graphics.fill(x, y, x + frameWidth, y + frameHeight, 0xFF211A22);
        graphics.fill(x + 1, y + 1, x + frameWidth - 1, y + frameHeight - 1, 0xFF4A4350);
        graphics.fill(x + 2, y + 2, x + frameWidth - 2, y + frameHeight - 2, 0xFF120C13);
        
        int fillY = y + frameHeight - 2 - filled;
        graphics.fill(x + 2, fillY, x + frameWidth - 2, y + frameHeight - 2, 0xFF7B2CBF);
        graphics.fill(x + 2, fillY, x + 4, y + frameHeight - 2, 0xFFB86BFF);
        
        String fearStr = Integer.toString(fear) + "%";
        if ("LEFT".equalsIgnoreCase(anchor)) {
            graphics.drawString(minecraft.font, "Fear", x + frameWidth + 4, y + 2, 0xD7C8DB, false);
            graphics.drawString(minecraft.font, fearStr, x + frameWidth + 4, y + frameHeight - 10, 0xD7C8DB, false);
        } else {
            graphics.drawString(minecraft.font, "Fear", x - minecraft.font.width("Fear") - 4, y + 2, 0xD7C8DB, false);
            graphics.drawString(minecraft.font, fearStr, x - minecraft.font.width(fearStr) - 4, y + frameHeight - 10, 0xD7C8DB, false);
        }
    }
}


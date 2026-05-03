package com.josem.echoofthevoid.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class FearBarOverlay {
    private FearBarOverlay() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || !ClientEffects.isFearBarEnabled()) {
            return;
        }

        int fear = ClientEffects.getFearLevel();
        int frameWidth = 84;
        int innerWidth = 80;
        int filled = Math.max(0, Math.min(innerWidth, Math.round(innerWidth * (fear / 100.0F))));
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() - 46;

        graphics.fill(x, y, x + frameWidth, y + 8, 0xFF211A22);
        graphics.fill(x + 1, y + 1, x + frameWidth - 1, y + 7, 0xFF4A4350);
        graphics.fill(x + 2, y + 2, x + frameWidth - 2, y + 6, 0xFF120C13);
        graphics.fill(x + 2, y + 2, x + 2 + filled, y + 6, 0xFF7B2CBF);
        graphics.fill(x + 2, y + 2, x + 2 + Math.min(filled, innerWidth), y + 3, 0xFFB86BFF);
        graphics.drawString(minecraft.font, "Fear", x, y - 9, 0xD7C8DB, false);
        graphics.drawString(minecraft.font, Integer.toString(fear), x + 68, y - 9, 0xD7C8DB, false);
    }
}

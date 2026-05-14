package com.josem.thewatcher.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FakeDisconnectScreen extends Screen {
    public FakeDisconnectScreen() {
        super(Component.translatable("screen.thewatcher.reconnecting"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("screen.thewatcher.reconnecting.detail"),
            this.width / 2,
            this.height / 2 + 10,
            0xA0A0A0
        );
    }
}


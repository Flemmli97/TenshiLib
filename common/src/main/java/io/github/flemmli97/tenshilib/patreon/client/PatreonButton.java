package io.github.flemmli97.tenshilib.patreon.client;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PatreonButton extends Button {

    private static final ResourceLocation TEXT = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/misc/tenshilib_patreon.png");

    public PatreonButton(int x, int y, Screen parent) {
        super(x, y, 20, 20, Component.literal(""), b -> Minecraft.getInstance().setScreen(new PatreonGui(parent)), DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = this.isHoveredOrFocused() ? 1 : 0;
        guiGraphics.blit(TEXT, this.getX(), this.getY(), 0, i * 20, this.width, this.height);
    }
}

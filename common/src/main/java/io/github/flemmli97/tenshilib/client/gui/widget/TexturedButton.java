package io.github.flemmli97.tenshilib.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Vanillas button has hardcoded sprite
 */
public class TexturedButton extends Button {

    private WidgetSprites sprites;

    public TexturedButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        this(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
    }

    public TexturedButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    public TexturedButton withSprite(WidgetSprites sprites) {
        this.sprites = sprites;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.sprites == null)
            return;
        Minecraft minecraft = Minecraft.getInstance();
        graphics.setColor(1, 1, 1, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.blitSprite(this.sprites.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        graphics.setColor(1, 1, 1, 1);
        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.renderString(graphics, minecraft.font, FastColor.ABGR32.color(Mth.ceil(this.alpha * 255.0F), color));
    }
}

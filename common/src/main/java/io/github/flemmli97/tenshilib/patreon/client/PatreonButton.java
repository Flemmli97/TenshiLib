package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PatreonButton extends Button {

    private static final ResourceLocation text = new ResourceLocation(TenshiLib.MODID, "textures/misc/tenshilib_patreon.png");

    public PatreonButton(int x, int y, Screen parent) {
        super(x, y, 20, 20, Component.literal(""), b -> Minecraft.getInstance().setScreen(new PatreonGui(parent)), DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, text);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int i = this.isHoveredOrFocused() ? 1 : 0;
        blit(poseStack, this.getX(), this.getY(), 0, i * 20, this.width, this.height);
    }
}

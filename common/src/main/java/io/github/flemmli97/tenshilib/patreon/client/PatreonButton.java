package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class PatreonButton extends Button {

    private static final ResourceLocation TEXT = new ResourceLocation(TenshiLib.MODID, "textures/misc/tenshilib_patreon.png");

    public PatreonButton(int x, int y, Screen parent) {
        super(x, y, 20, 20, new TextComponent(""), b -> Minecraft.getInstance().setScreen(new PatreonGui(parent)));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXT);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int i = this.isHoveredOrFocused() ? 1 : 0;
        this.blit(poseStack, this.x, this.y, 0, i * 20, this.width, this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
    }
}

package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.client.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class HorizontalColorSlider extends AbstractWidget {

    private final Color firstColor, secondColor;
    private final Color.MutableColor color;
    private final Consumer<HorizontalColorSlider> onUpdate;

    public HorizontalColorSlider(int x, int y, int width, int height, Color firstColor, Color secondColor, Consumer<HorizontalColorSlider> onUpdate, Component title) {
        super(x, y, width, height, title);
        this.firstColor = firstColor;
        this.secondColor = secondColor;
        this.color = new Color.MutableColor(this.firstColor.hex(), true);
        this.onUpdate = onUpdate;
    }

    public Color getColor() {
        return this.color;
    }

    public void with(int color) {
        Color newColor = new Color(color, false);
        this.color.setRGB(Mth.clamp(newColor.getRed(), this.firstColor.getRed(), this.secondColor.getRed()),
                Mth.clamp(newColor.getGreen(), this.firstColor.getGreen(), this.secondColor.getGreen()),
                Mth.clamp(newColor.getBlue(), this.firstColor.getBlue(), this.secondColor.getBlue()),
                Mth.clamp(newColor.getAlpha(), this.firstColor.getAlpha(), this.secondColor.getAlpha()));
    }

    public void setColor(int red, int green, int blue, int alpha) {
        int hex = this.color.hex();
        this.color.setRGB(Mth.clamp(red, this.firstColor.getRed(), this.secondColor.getRed()),
                Mth.clamp(green, this.firstColor.getGreen(), this.secondColor.getGreen()),
                Mth.clamp(blue, this.firstColor.getBlue(), this.secondColor.getBlue()),
                Mth.clamp(alpha, this.firstColor.getAlpha(), this.secondColor.getAlpha()));
        if (hex != this.color.hex())
            this.onUpdate.accept(this);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, -1);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, PatreonGui.BLACK);
        fillHorizontalGradient(guiGraphics, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.firstColor.hex(), this.secondColor.hex());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        double percent = Mth.clamp((mouseX - this.getX()) / this.width, 0, 1);
        this.setColor((int) ((this.secondColor.getRed() - this.firstColor.getRed()) * percent),
                (int) ((this.secondColor.getGreen() - this.firstColor.getGreen()) * percent),
                (int) ((this.secondColor.getBlue() - this.firstColor.getBlue()) * percent),
                (int) ((this.secondColor.getAlpha() - this.firstColor.getAlpha()) * percent));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        double percent = Mth.clamp((mouseX - this.getX()) / this.width, 0, 1);
        this.setColor((int) ((this.secondColor.getRed() - this.firstColor.getRed()) * percent),
                (int) ((this.secondColor.getGreen() - this.firstColor.getGreen()) * percent),
                (int) ((this.secondColor.getBlue() - this.firstColor.getBlue()) * percent),
                (int) ((this.secondColor.getAlpha() - this.firstColor.getAlpha()) * percent));
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    protected static void fillHorizontalGradient(GuiGraphics poseStack, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        fillHorizontalGradient(poseStack.pose().last().pose(), poseStack.bufferSource().getBuffer(RenderType.gui()), x1, y1, x2, y2, colorFrom, colorTo);
    }

    protected static void fillHorizontalGradient(Matrix4f matrix, VertexConsumer builder, int x1, int y1, int x2, int y2, int colorA, int colorB) {
        float f = (float) (colorA >> 24 & 0xFF) / 255.0f;
        float g = (float) (colorA >> 16 & 0xFF) / 255.0f;
        float h = (float) (colorA >> 8 & 0xFF) / 255.0f;
        float i = (float) (colorA & 0xFF) / 255.0f;
        float j = (float) (colorB >> 24 & 0xFF) / 255.0f;
        float k = (float) (colorB >> 16 & 0xFF) / 255.0f;
        float l = (float) (colorB >> 8 & 0xFF) / 255.0f;
        float m = (float) (colorB & 0xFF) / 255.0f;
        builder.addVertex(matrix, x2, y1, 0).setColor(k, l, m, j);
        builder.addVertex(matrix, x1, y1, 0).setColor(g, h, i, f);
        builder.addVertex(matrix, x1, y2, 0).setColor(g, h, i, f);
        builder.addVertex(matrix, x2, y2, 0).setColor(k, l, m, j);
    }
}

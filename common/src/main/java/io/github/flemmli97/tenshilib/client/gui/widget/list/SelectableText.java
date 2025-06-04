package io.github.flemmli97.tenshilib.client.gui.widget.list;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.client.gui.widget.TextureLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.BooleanSupplier;

public class SelectableText implements SelectableEntry {

    private final Component text;

    private int paddingX = 4;
    private SelectButton[] button;
    private Pair<TextureLocation, TextureLocation> entryTexture;
    private boolean selectable = true;

    private FormattedCharSequence sequence;
    private FormattedCharSequence blankSequence;
    private int lastWidth, width, height;

    public SelectableText(String text, ChatFormatting... chatFormattings) {
        this(new TextComponent(text).withStyle(chatFormattings));
    }

    public SelectableText(Component text) {
        this.text = text;
    }

    public SelectableText padding(int padding) {
        this.paddingX = padding;
        return this;
    }

    public SelectableText with(SelectButton... clickHandler) {
        return this.with(false, clickHandler);
    }

    public SelectableText with(boolean canSelect, SelectButton... clickHandler) {
        this.selectable = canSelect;
        this.button = clickHandler;
        return this;
    }

    public SelectableText withTexture(ResourceLocation background, Pair<TextureLocation, TextureLocation> entryTexture) {
        this.entryTexture = entryTexture;
        return this;
    }

    public SelectableText noSelect() {
        this.selectable = false;
        return this;
    }

    @Override
    public void updateDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(SelectableListWidget widget, PoseStack poseStack, int mouseX, int mouseY, float partialTick,
                       int x, int y, boolean selected, boolean hovered) {
        if (this.entryTexture != null) {
            TextureLocation location = hovered ? this.entryTexture.getSecond() : this.entryTexture.getFirst();
            RenderSystem.setShaderTexture(0, location.texture());
            widget.blit(poseStack, x, y, location.uOffset(), location.vOffset(), this.width, this.height);
        } else if (selected || hovered) {
            GuiComponent.fill(poseStack, x, y, x + this.width, y + this.height, 0xa0101010);
        }

        int textWidth = this.width - this.paddingX;
        if (this.getButtons() != null && this.getButtons().length > 0) {
            int skipped = 0;
            for (int idx = 0; idx < this.getButtons().length; idx++) {
                SelectButton btn = this.getButtons()[(this.getButtons().length - 1) - idx];
                if (!btn.shouldRender().getAsBoolean()) {
                    ++skipped;
                    continue;
                }
                int[] xY = this.getButtonStart(idx - skipped);
                int bX = xY[0] + x;
                int bY = xY[1] + y;
                textWidth = xY[0] - 2;
                RenderSystem.setShaderTexture(0, btn.texture());
                boolean over = mouseX >= bX && mouseY >= bY && mouseX < bX + 12 && mouseY < bY + 12;
                widget.blit(poseStack, bX, bY, btn.uOffset(), btn.vOffset() + (over ? 12 : 0), 12, 12);
            }
        }
        widget.getFont().draw(poseStack, this.getText(widget.getFont(), textWidth - this.paddingX, selected || hovered),
                x + this.paddingX, y + (int) (0.5 * this.height - 3.5),
                selected ? ChatFormatting.LIGHT_PURPLE.getColor() : hovered ? ChatFormatting.YELLOW.getColor() : 0xFFFFFF);
    }

    @Override
    public boolean onClick(double relativeMouseX, double relativeMouseY, boolean selected) {
        if (this.getButtons() != null && this.getButtons().length > 0) {
            int skipped = 0;
            for (int idx = 0; idx < this.getButtons().length; idx++) {
                SelectButton btn = this.getButtons()[(this.getButtons().length - 1) - idx];
                if (!btn.shouldRender().getAsBoolean()) {
                    ++skipped;
                }
                int[] xY = this.getButtonStart(idx - skipped);
                boolean over = relativeMouseX >= xY[0] && relativeMouseY >= xY[1] && relativeMouseX < xY[0] + 12 && relativeMouseY < xY[1] + 12;
                if (over) {
                    btn.onClick().run();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    return false;
                }
            }
        }
        return this.selectable;
    }

    public FormattedCharSequence getText(Font font, int width, boolean highlight) {
        if (this.sequence == null || this.lastWidth != width) {
            this.sequence = SelectableEntry.dottedWith(font, this.text, width, false);
            this.blankSequence = SelectableEntry.dottedWith(font, this.text, width, true);
            this.lastWidth = width;
        }
        return highlight ? this.blankSequence : this.sequence;
    }

    public SelectButton[] getButtons() {
        return this.button;
    }

    private int[] getButtonStart(int position) {
        int btnX = this.width - this.paddingX - 12 - position * 14;
        int btnY = (int) (0.5 * this.height) - 6;
        return new int[]{btnX, btnY};
    }

    public record SelectButton(ResourceLocation texture, int uOffset, int vOffset, Runnable onClick,
                               BooleanSupplier shouldRender) {

        private static final BooleanSupplier ALWAYS = () -> true;

        public SelectButton(ResourceLocation texture, int uOffset, int vOffset, Runnable onClick) {
            this(texture, uOffset, vOffset, onClick, ALWAYS);
        }
    }
}

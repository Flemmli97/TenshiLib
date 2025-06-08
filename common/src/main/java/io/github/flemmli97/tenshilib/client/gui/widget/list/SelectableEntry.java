package io.github.flemmli97.tenshilib.client.gui.widget.list;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public interface SelectableEntry {

    static FormattedCharSequence dottedWith(Font font, Component text, int maxWidth, boolean noStyle) {
        if (noStyle && text instanceof MutableComponent mut)
            text = mut.copy().setStyle(Style.EMPTY);
        int w = font.width(text);
        if (w > maxWidth) {
            int dots = font.width("...");
            FormattedText sub = font.substrByWidth(text, maxWidth - dots);
            return FormattedCharSequence.composite(Language.getInstance().getVisualOrder(sub), Component.literal("...").withStyle(text.getStyle()).getVisualOrderText());
        }
        return text.getVisualOrderText();
    }

    void updateDimensions(int width, int height);

    void render(SelectableListWidget widget, GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int x, int y, boolean selected, boolean hovered);

    default boolean onClick(double relativeMouseX, double relativeMouseY, boolean selected) {
        return true;
    }

    default void unSelect() {
    }
}

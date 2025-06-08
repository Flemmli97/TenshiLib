package io.github.flemmli97.tenshilib.client.gui.widget.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.List;


public class SelectableListWidget extends AbstractWidget {

    private final Font font;
    private final List<SelectableEntry> entries;
    private final boolean[] selected;
    private int entryHeight, limit;

    private ResourceLocation background;
    private int paddingY = 4;

    private boolean canSelectMultiple;
    private int offset;
    private int hovered, lastSelect;

    public SelectableListWidget(int x, int y, int width, int height, Font font, List<SelectableEntry> entries) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.entries = entries;
        this.selected = new boolean[this.entries.size()];
        this.entryHeight = this.font.lineHeight + 3 + this.paddingY;
        this.entries.forEach(e -> e.updateDimensions(this.width, this.entryHeight));
        this.limit = height / this.entryHeight;
    }

    public SelectableListWidget withPadding(int paddingY) {
        this.paddingY = paddingY;
        this.entryHeight = this.font.lineHeight + 3 + this.paddingY;
        this.entries.forEach(e -> e.updateDimensions(this.width, this.entryHeight));
        this.limit = this.height / this.entryHeight;
        return this;
    }

    public SelectableListWidget withTexture(ResourceLocation background) {
        this.background = background;
        return this;
    }

    public SelectableListWidget selectMultiple() {
        this.canSelectMultiple = true;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.background != null) {
            graphics.blit(this.background, this.getX(), this.getY(), 0, 0, this.width, this.height);
        }
        this.hoverOver(this.isHovered ? this.indexFromMouse(mouseY) : -1);
        for (int i = 0; i < this.entries.size(); i++) {
            int idxx = (this.offset + i) % this.entries.size();
            if (i >= this.limit || idxx >= this.entries.size())
                break;
            SelectableEntry entry = this.entries.get(idxx);
            boolean selected = this.selected[idxx];
            boolean hovered = this.hovered == idxx;
            int entryY = this.getY() + i * this.entryHeight;
            entry.render(this, graphics, mouseX, mouseY, partialTick, this.getX(), entryY, selected, hovered);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            return false;
        }
        int i = this.indexFromMouse(mouseY);
        if (i != -1) {
            this.hoverOver(i);
            int entryX = this.getX();
            int entryY = this.getY() + i * this.entryHeight;
            double relMouseX = mouseX - entryX;
            double relMouseY = mouseY - entryY;
            if (!this.canSelectMultiple && this.lastSelect != this.hovered) {
                this.selected[this.lastSelect] = false;
                this.entries.get(this.lastSelect).unSelect();
            }
            this.lastSelect = this.hovered;
            boolean selected = this.selected[this.hovered];
            this.selected[this.hovered] = !this.selected[this.hovered];
            SelectableEntry entry = this.entries.get(this.hovered);
            if (selected && !this.selected[this.hovered])
                entry.unSelect();
            if (!entry.onClick(relMouseX, relMouseY, this.selected[this.hovered]))
                this.selected[this.hovered] = false;
            else {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
        }
        return true;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY) && this.indexFromMouse(mouseY) != -1;
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    private int indexFromMouse(double mouseY) {
        double relativePos = mouseY - this.getY();
        if (relativePos < 0 || relativePos > this.getY() + this.height)
            return -1;
        int idx = (int) (relativePos / this.entryHeight + this.offset);
        if (idx >= this.entries.size() || idx >= this.offset + this.limit)
            return -1;
        return idx;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (this.isHovered) {
            this.offset = Mth.clamp((int) (this.offset - scrollY), 0, Math.max(this.entries.size() - this.limit, 0));
            return true;
        }
        return false;
    }

    public void hoverOver(int index) {
        this.hovered = index;
        if (this.hovered != -1) {
            if (this.hovered < 0) {
                this.hovered += this.entries.size();
            }
            if (this.hovered >= this.entries.size()) {
                this.hovered -= this.entries.size();
            }
        }
    }

    public Font getFont() {
        return this.font;
    }
}

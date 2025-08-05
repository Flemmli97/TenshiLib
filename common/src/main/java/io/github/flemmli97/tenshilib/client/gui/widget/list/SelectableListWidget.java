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
    private Scrollbar scrollbar;

    private boolean canSelectMultiple;
    private int offset;
    private int hovered, lastSelect;

    private boolean mouseScrollDragging;

    public SelectableListWidget(int x, int y, int width, int height, Font font, List<SelectableEntry> entries) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.entries = entries;
        this.selected = new boolean[this.entries.size()];
        this.setEntryHeight(this.font.lineHeight + 3, this.paddingY);
    }

    public SelectableListWidget setEntryHeight(int height, int paddingY) {
        this.entryHeight = height;
        this.entries.forEach(e -> e.updateDimensions(this.getEntryWidth(), this.entryHeight));
        return this.withPadding(paddingY);
    }

    public SelectableListWidget withPadding(int paddingY) {
        this.paddingY = paddingY;
        this.limit = (this.height + this.paddingY) / (this.entryHeight + this.paddingY);
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

    public SelectableListWidget scrollbar(Scrollbar scrollbar) {
        this.scrollbar = scrollbar;
        this.entries.forEach(e -> e.updateDimensions(this.getEntryWidth(), this.entryHeight));
        return this;
    }

    protected int getEntryWidth() {
        return this.getWidth() - (this.scrollbar != null ? this.scrollbar.totalWidth() : 0);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.background != null) {
            graphics.blitSprite(this.background, this.getX(), this.getY(), this.width, this.height);
        }
        this.hoverOver(this.isHovered ? this.indexFromMouse(mouseX, mouseY) : -1);
        for (int i = 0; i < this.entries.size(); i++) {
            int idxx = (this.offset + i) % this.entries.size();
            if (i >= this.limit || idxx >= this.entries.size())
                break;
            SelectableEntry entry = this.entries.get(idxx);
            boolean selected = this.selected[idxx];
            boolean hovered = this.hovered == idxx;
            int entryY = this.getY() + i * (this.entryHeight + this.paddingY);
            entry.render(this, graphics, mouseX, mouseY, partialTick, this.getX(), entryY, selected, hovered);
        }
        if (this.scrollbar != null) {
            int scrollbarX = this.scrollbarDims()[0];
            if (this.entries.size() <= this.limit) {
                graphics.blitSprite(this.scrollbar.disabled(),
                        scrollbarX, this.getY() + this.scrollbar.topPadding(),
                        this.scrollbar.width(), this.scrollbar.height());
            } else {
                float relative = (float) this.offset / Math.max(0, this.entries.size() - this.limit);
                int y = this.getY() + (int) ((this.getHeight() - this.scrollbar.totalHeight()) * relative);
                graphics.blitSprite(this.scrollbar.texture(),
                        scrollbarX, y + this.scrollbar.topPadding(),
                        this.scrollbar.width(), this.scrollbar.height());
            }
        }
    }

    protected int[] scrollbarDims() {
        if (this.scrollbar == null)
            return null;
        int minX = this.getX() + this.getWidth() - this.scrollbar.totalWidth() + this.scrollbar.leftPadding();
        int maxX = minX + this.scrollbar.width();
        int minY = this.getY() + this.scrollbar.topPadding();
        int maxY = minY + this.getHeight() - this.scrollbar.bottomPadding();
        return new int[]{minX, minY, maxX, maxY};
    }

    protected boolean inScrollBar(double mouseX, double mouseY) {
        if (this.scrollbar == null)
            return false;
        int[] dim = this.scrollbarDims();
        return mouseX >= dim[0] && mouseY >= dim[1] && mouseX < dim[2] && mouseY < dim[3];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            return false;
        }
        if (this.inScrollBar(mouseX, mouseY)) {
            this.mouseScrollDragging = true;
            return true;
        }
        int i = this.indexFromMouse(mouseX, mouseY);
        if (i != -1) {
            this.hoverOver(i);
            int entryX = this.getX();
            int entryY = this.getY() + i * (this.entryHeight + this.paddingY);
            double relMouseX = mouseX - entryX;
            double relMouseY = mouseY - entryY;
            SelectableEntry entry = this.select(this.hovered, true);
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
        return super.clicked(mouseX, mouseY) && (this.indexFromMouse(mouseX, mouseY) != -1 || this.inScrollBar(mouseX, mouseY));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.mouseScrollDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.mouseScrollDragging) {
            int[] dims = this.scrollbarDims();
            double relativePos = mouseY - dims[1];
            int height = dims[3] - dims[1];
            double relative = relativePos / height;
            int max = Math.max(this.entries.size() - this.limit, 0);
            this.offset = Mth.clamp((int) Math.round(relative * max), 0, max);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean isMouseDragging() {
        return this.mouseScrollDragging;
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

    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    private int indexFromMouse(double mouseX, double mouseY) {
        if (mouseX >= this.getX() + this.getEntryWidth())
            return -1;
        double relativePos = mouseY - this.getY();
        if (relativePos < 0 || relativePos > this.getY() + this.height)
            return -1;
        int idx = (int) (relativePos / (this.entryHeight + this.paddingY) + this.offset);
        if (idx >= this.entries.size() || idx >= this.offset + this.limit)
            return -1;
        // Ignore padding space
        int entryY = (idx - this.offset) * (this.entryHeight + this.paddingY);
        if (relativePos < entryY || relativePos > entryY + this.entryHeight)
            return -1;
        return idx;
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

    public SelectableEntry select(int select, boolean toggle) {
        if (select < 0 || select >= this.entries.size())
            return null;
        if (!this.canSelectMultiple && this.lastSelect != select) {
            this.selected[this.lastSelect] = false;
            this.entries.get(this.lastSelect).unSelect();
        }
        this.lastSelect = select;
        boolean previous = this.selected[select];
        this.selected[select] = !toggle || !this.selected[select];
        SelectableEntry entry = this.entries.get(select);
        if (previous && !this.selected[select])
            entry.unSelect();
        return entry;
    }

    public void scrollTo(int offset) {
        this.offset = Mth.clamp(offset, 0, Math.max(this.entries.size() - this.limit, 0));
    }

    public int getScrollValue() {
        return this.offset;
    }

    public Font getFont() {
        return this.font;
    }

    public record Scrollbar(ResourceLocation texture, ResourceLocation disabled,
                            int width, int height, int leftPadding, int rightPadding, int topPadding,
                            int bottomPadding) {

        public Scrollbar(ResourceLocation texture, ResourceLocation disabled, int width, int height) {
            this(texture, disabled, width, height, 0, 0, 0, 0);
        }

        public Scrollbar(ResourceLocation texture, ResourceLocation disabled, int width, int height, int leftPadding, int topPadding) {
            this(texture, disabled, width, height, leftPadding, 0, topPadding, 0);
        }

        public int totalWidth() {
            return this.width() + this.leftPadding() + this.rightPadding();
        }

        public int totalHeight() {
            return this.height() + this.topPadding() + this.bottomPadding();
        }
    }
}

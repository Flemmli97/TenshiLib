package io.github.flemmli97.tenshilib.client.gui;

import io.github.flemmli97.tenshilib.client.gui.widget.SuggestionEditBox;
import io.github.flemmli97.tenshilib.client.render.RenderUtils;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.network.C2SAnimationDebuggerUpdate;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class AnimationScreen<T extends LivingEntity & AnimatedEntity> extends Screen {

    protected final T entity;
    private final InteractionHand hand;
    private final String[] animations;

    private int leftPos, topPos;
    private final int sizeX = 240;
    private final int sizeY = 160;

    private SuggestionEditBox box;

    private String selected;

    public AnimationScreen(T entity, InteractionHand hand, String id) {
        super(Component.translatable("tenshilib.gui.animation"));
        this.entity = entity;
        this.animations = entity.getAnimationHandler().getAnimations().all()
                .toArray(String[]::new);
        this.hand = hand;
        this.selected = id;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = this.width / 2 - (this.sizeX / 2);
        this.topPos = this.height / 2 - (this.sizeY / 2);
        this.buttons();
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fillGradient(this.leftPos, this.topPos, this.leftPos + this.sizeX, this.topPos + this.sizeY, 0xc0101010, 0xc0101010);
        int padding = 16;
        float scale = 32;
        RenderUtils.renderScaledEntityGui(graphics, this.leftPos + this.sizeX - padding - (3 * scale), this.topPos + padding,
                3 * scale, 3 * scale, scale, 0, mouseX, mouseY, this.entity);
        graphics.drawString(this.font, this.getTitle(), this.leftPos + 16, this.topPos + 16, 0xffffff);
    }

    protected void buttons() {
        int padding = 16;
        int yOff = padding + 12 + 20 * 4;

        this.box = new SuggestionEditBox(this.minecraft.font, this.leftPos + this.sizeX / 2 - 70, this.topPos + yOff, 140, 20, Component.empty(),
                5, true, SuggestionEditBox.ofString(List.of(this.animations)));
        this.box.setValue(this.selected);
        this.box.setResponder(s -> {
            s = s.trim();
            this.box.setTextColor(0xFF0000);
            for (String str : this.animations) {
                if (s.equals(str)) {
                    this.box.setTextColor(0xE0E0E0);
                    this.selected = s;
                    break;
                }
            }
        });
        yOff += 24;
        this.addRenderableWidget(Button.builder(Component.translatable("tenshilib.gui.save"), b -> {
            LoaderNetwork.INSTANCE.sendToServer(new C2SAnimationDebuggerUpdate(this.hand, this.selected));
            this.minecraft.setScreen(null);
        }).bounds(this.leftPos + this.sizeX / 2 - 50, this.topPos + yOff, 100, 20).build());
        this.addRenderableWidget(this.box);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.box.canConsumeInput() && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean click = super.mouseClicked(mouseX, mouseY, button);
        if (!click)
            this.setFocused(null);
        return click;
    }
}

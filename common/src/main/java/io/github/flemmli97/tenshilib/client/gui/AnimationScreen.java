package io.github.flemmli97.tenshilib.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.client.gui.widget.SuggestionEditBox;
import io.github.flemmli97.tenshilib.common.network.C2SAnimationDebuggerUpdate;
import io.github.flemmli97.tenshilib.common.network.NetworkCrossPlat;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

public class AnimationScreen<T extends LivingEntity & IAnimated> extends Screen {

    protected final T entity;
    private final InteractionHand hand;
    private final String[] animations;

    private int leftPos, topPos;
    private final int sizeX = 240;
    private final int sizeY = 160;

    private SuggestionEditBox box;

    private int index;

    public AnimationScreen(T entity, InteractionHand hand, int index) {
        super(new TranslatableComponent("tenshilib.gui.animation"));
        this.entity = entity;
        this.animations = Arrays.stream(entity.getAnimationHandler().getAnimations()).map(AnimatedAction::getID)
                .toArray(String[]::new);
        this.hand = hand;
        this.index = Mth.clamp(index, 0, this.animations.length - 1);
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.leftPos = this.width / 2 - (this.sizeX / 2);
        this.topPos = this.height / 2 - (this.sizeY / 2);
        this.buttons();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(stack, this.leftPos, this.topPos, this.leftPos + this.sizeX, this.topPos + this.sizeY, 0xc0101010, 0xc0101010);
        int posX = 180;
        int posY = 90;
        float scale = 1;
        if (this.entity.getBbWidth() > 1.2) {
            scale = 2f / this.entity.getBbWidth();
        }
        if (this.entity.getBbHeight() > 1.6) {
            scale = Math.min(scale, 2.4f / this.entity.getBbHeight());
        }
        InventoryScreen.renderEntityInInventory(this.leftPos + posX, this.topPos + posY, (int) (32 * scale), this.leftPos + posX - mouseX, this.topPos + (posY - 35) - mouseY, this.entity);
        this.minecraft.font.draw(stack, this.getTitle(), this.leftPos + 16, this.topPos + 16, 0xffffff);
        super.render(stack, mouseX, mouseY, partialTick);
    }

    protected void buttons() {
        int padding = 16;
        int yOff = padding + 12 + 20 * 4;

        this.box = new SuggestionEditBox(this.minecraft.font, this.leftPos + this.sizeX / 2 - 70, this.topPos + yOff, 140, 20, new TranslatableComponent("fateubw.gui.animation"),
                5, true, SuggestionEditBox.ofString(List.of(this.animations)));
        this.box.setValue(this.index >= 0 ? this.animations[this.index] : "");
        this.box.setResponder(s -> {
            s = s.trim();
            this.box.setTextColor(0xFF0000);
            for (int i = 0; i < this.animations.length; i++) {
                String str = this.animations[i];
                if (s.equals(str)) {
                    this.box.setTextColor(0xE0E0E0);
                    this.index = i;
                    break;
                }
            }
        });
        yOff += 24;
        this.addRenderableWidget(new Button(this.leftPos + this.sizeX / 2 - 50, this.topPos + yOff, 100, 20, new TranslatableComponent("tenshilib.gui.save"), b -> {
            NetworkCrossPlat.INSTANCE.sendToServer(new C2SAnimationDebuggerUpdate(this.hand, this.index));
            this.minecraft.setScreen(null);
        }));
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
}

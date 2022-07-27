package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.client.render.RenderUtils;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonEffects;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Function;

public class PatreonGui extends Screen {

    private final Screen parent;

    private PatreonEffects.PatreonEffectConfig effect;
    private RenderLocation renderLocation;
    private boolean render = true;
    private int color = RenderUtils.defaultColor;

    private int tier;

    private CycleButton<RenderLocation> locationButton;

    public PatreonGui(Screen screen) {
        super(Component.translatable("tenshilib.patreon.title").withStyle(ChatFormatting.GOLD));
        this.parent = screen;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        Component name;
        this.tier = this.minecraft.level == null ? -1 : PatreonDataManager.get(this.minecraft.player.getUUID().toString()).tier();
        if (this.tier < 1) {
            if (this.tier == -1)
                name = Component.translatable("tenshilib.patreon.level.no");
            else
                name = Component.translatable("tenshilib.patreon.back");
        } else {
            name = CommonComponents.GUI_DONE;
            this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 24 * 6, 200, 20, Component.translatable("tenshilib.patreon.save"), button -> PatreonClientPlatform.INSTANCE.sendToServer(new C2SEffectUpdatePkt(this.effect.id(), this.render, this.renderLocation, this.color))));
        }
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 24 * 7, 200, 20, name, button -> this.minecraft.setScreen(this.parent)));
        if (this.tier < 1)
            return;
        PatreonPlatform.INSTANCE.playerSettings(this.minecraft.player)
                .ifPresent(setting -> {
                    this.effect = setting.effect();
                    if (this.effect == null)
                        this.effect = PatreonEffects.get(PatreonDataManager.get(this.minecraft.player.getUUID().toString()).defaultEffect());
                    this.renderLocation = setting.getRenderLocation();
                    if (this.effect != null && !this.effect.locationAllowed(this.renderLocation))
                        this.renderLocation = this.effect.defaultLoc();
                    this.render = setting.shouldRender();
                    this.color = setting.getColor();

                    Function<PatreonEffects.PatreonEffectConfig, Component> idF = eff -> Component.translatable("tenshilib.patreon.id." + eff.id());
                    List<PatreonEffects.PatreonEffectConfig> effects = new ArrayList<>();
                    for (PatreonEffects.PatreonEffectConfig eff : PatreonEffects.allEffects())
                        if (eff.tier <= this.tier)
                            effects.add(eff);
                    this.addRenderableWidget(CycleButton.builder(idF).withValues(effects)
                            .withInitialValue(this.effect)
                            .create(this.width / 2 - 125, this.height / 6 + 24, 250, 20,
                                    Component.translatable("tenshilib.patreon.id"), (cycleButton, eff) -> {
                                        this.effect = eff;
                                        if (!this.effect.locationAllowed(this.renderLocation)) {
                                            this.renderLocation = this.effect.defaultLoc();
                                        }
                                        this.setLocationButton();
                                    }));
                    this.addRenderableWidget(CycleButton.onOffBuilder(this.render).create(this.width / 2 - 125, this.height / 6 + 24 * 2, 250, 20,
                            Component.translatable("tenshilib.patreon.render"), (cycleButton, boolean_) -> this.render = boolean_));

                    this.setLocationButton();

                    EditBox txtField = new EditBox(this.minecraft.font, this.width / 2 - 155 + 160, this.height / 6 + 24 * 4, 150, 20, Component.translatable("tenshilib.patreon.color")) {
                        @Override
                        public boolean charTyped(char codePoint, int modifiers) {
                            if (this.getValue().length() > 8)
                                return false;
                            return HexFormat.isHexDigit(codePoint) && super.charTyped(codePoint, modifiers);
                        }
                    };
                    txtField.setValue(HexFormat.of().toHexDigits(this.color));
                    txtField.setResponder(s -> {
                        if (s.isEmpty())
                            this.color = RenderUtils.defaultColor;
                        else
                            this.color = HexFormat.fromHexDigits(s);
                        if (this.color == 0 || (txtField.getValue().length() != 6 && txtField.getValue().length() != 8))
                            txtField.setTextColor(RenderUtils.defaultColor);
                        else
                            txtField.setTextColor(this.color);
                    });
                    this.addRenderableWidget(txtField);
                });
    }

    private void setLocationButton() {
        if (this.locationButton != null)
            this.removeWidget(this.locationButton);
        List<RenderLocation> allowed = new ArrayList<>();
        for (RenderLocation l : RenderLocation.values())
            if (this.effect == null || this.effect.locationAllowed(l))
                allowed.add(l);
        Function<RenderLocation, Component> f = loc -> Component.translatable("tenshilib.patreon.location." + loc.toString());
        this.addRenderableWidget(this.locationButton = CycleButton.builder(f).withValues(allowed)
                .withInitialValue(this.renderLocation)
                .create(this.width / 2 - 125, this.height / 6 + 24 * 3, 250, 20,
                        Component.translatable("tenshilib.patreon.location"), (cycleButton, loc) -> this.renderLocation = loc));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        GuiComponent.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        if (this.tier == 0) {
            GuiComponent.drawCenteredString(poseStack, this.font, Component.translatable("tenshilib.patreon.not").withStyle(ChatFormatting.DARK_RED), this.width / 2, 50, 0xFFFFFF);
        }
        if (this.tier > 0) {
            int ex = this.width / 2 - 220;
            int ey = this.height / 6 + 24 * 7;
            InventoryScreen.renderEntityInInventory(ex, ey, 65, ex - mouseX, ey - 83 - mouseY, this.minecraft.player);
            GuiComponent.drawCenteredString(poseStack, this.font, Component.translatable("tenshilib.patreon.color"), this.width / 2 - 55, this.height / 6 + 24 * 4 + 8, 0xFFFFFF);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}

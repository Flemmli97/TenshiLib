package io.github.flemmli97.tenshilib.patreon.client;

import io.github.flemmli97.tenshilib.client.Color;
import io.github.flemmli97.tenshilib.client.render.RenderUtils;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.effects.GuiElement;
import io.github.flemmli97.tenshilib.patreon.effects.PatreonEffectConfig;
import io.github.flemmli97.tenshilib.patreon.effects.PatreonEffects;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SRequestUpdateClientPkt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    public static final int BLACK = 0xFF000000;

    private final Screen parent;

    private PatreonEffectConfig effect;
    private RenderLocation renderLocation;
    private boolean render = true;
    private int color = RenderUtils.DEFAULT_COLOR;

    private EditBox txtField;
    private HorizontalColorSlider red, green, blue, alpha;

    private int tier;

    private CycleButton<RenderLocation> locationButton;
    private PatreonPlayerSetting setting;

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
        this.clearWidgets();
        Component name;
        this.tier = this.minecraft.level == null ? -1 : PatreonDataManager.get(this.minecraft.player.getUUID().toString()).tier();
        if (this.tier < 1) {
            if (this.tier == -1)
                name = Component.translatable("tenshilib.patreon.level.no");
            else
                name = Component.translatable("tenshilib.patreon.back");
            this.addRenderableWidget(Button.builder(name, button -> this.minecraft.setScreen(this.parent))
                    .pos(this.width / 2 - 100, this.height / 8 + 24 * 8).size(200, 20).build());
            return;
        }
        name = CommonComponents.GUI_DONE;
        this.setting = PatreonPlatform.INSTANCE.playerSettings(this.minecraft.player);
        if (this.setting == null) {
            this.addRenderableWidget(Button.builder(Component.translatable("tenshilib.patreon.save"), button -> PatreonClientPlatform.INSTANCE.sendToServer(new C2SEffectUpdatePkt(this.effect.id(), this.render, this.renderLocation, this.color)))
                    .pos(this.width / 2 - 100, this.height / 8 + 24 * 7).size(200, 20).build());
            this.addRenderableWidget(Button.builder(name, button -> this.minecraft.setScreen(this.parent))
                    .pos(this.width / 2 - 100, this.height / 8 + 24 * 8).size(200, 20).build());
            return;
        }
        int yOffset = 0;
        this.effect = this.setting.effect();
        if (this.effect == null)
            this.effect = PatreonEffects.get(PatreonDataManager.get(this.minecraft.player.getUUID().toString()).defaultEffect());
        this.renderLocation = this.setting.getRenderLocation();
        if (this.effect != null && !this.effect.locationAllowed(this.renderLocation))
            this.renderLocation = this.effect.defaultLoc();
        this.render = this.setting.shouldRender();
        this.color = this.setting.getColor();

        Function<PatreonEffectConfig, Component> idF = eff -> Component.translatable("tenshilib.patreon.id." + eff.id());
        List<PatreonEffectConfig> effects = new ArrayList<>();
        for (PatreonEffectConfig eff : PatreonEffects.allEffects())
            if (eff.tier <= this.tier)
                effects.add(eff);
        this.addRenderableWidget(CycleButton.builder(idF).withValues(effects)
                .withInitialValue(this.effect)
                .create(this.width / 2 - 125, this.height / 8, 250, 20,
                        Component.translatable("tenshilib.patreon.id"), (cycleButton, eff) -> {
                            this.effect = eff;
                            if (!this.effect.locationAllowed(this.renderLocation)) {
                                this.renderLocation = this.effect.defaultLoc();
                            }
                            this.update();
                            this.init();
                        }));
        yOffset += 24;
        this.addRenderableWidget(CycleButton.onOffBuilder(this.render).create(this.width / 2 - 125, this.height / 8 + yOffset, 250, 20,
                Component.translatable("tenshilib.patreon.render"), (cycleButton, boolean_) -> {
                    this.render = boolean_;
                    this.update();
                }));
        yOffset += 24;
        yOffset += this.setLocationButton();

        if (this.effect != null && this.effect.guiElements().contains(GuiElement.COLOR)) {
            this.addRenderableWidget(this.red = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + yOffset, 250, 14,
                    new Color(0, true), new Color(255, 0, 0), slider -> this.updateColorString(),
                    Component.translatable("tenshilib.patreon.slider.red")));
            this.red.with(this.color);
            yOffset += 18;
            this.addRenderableWidget(this.green = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + yOffset, 250, 14,
                    new Color(0, true), new Color(0, 255, 0), slider -> this.updateColorString(),
                    Component.translatable("tenshilib.patreon.slider.green")));
            this.green.with(this.color);
            yOffset += 18;
            this.addRenderableWidget(this.blue = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + yOffset, 250, 14,
                    new Color(0, true), new Color(0, 0, 255), slider -> this.updateColorString(),
                    Component.translatable("tenshilib.patreon.slider.blue")));
            this.blue.with(this.color);
            yOffset += 18;
            this.addRenderableWidget(this.alpha = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + yOffset, 250, 14,
                    new Color(0, false), new Color(255, 255, 255), slider -> this.updateColorString(),
                    Component.translatable("tenshilib.patreon.slider.alpha")));
            this.alpha.with(this.color);
            yOffset += 24;
            this.txtField = new EditBox(this.minecraft.font, this.width / 2 - 155 + 160, this.height / 8 + 24 * 6, 150, 20, Component.translatable("tenshilib.patreon.color")) {
                @Override
                public boolean charTyped(char codePoint, int modifiers) {
                    if (this.getValue().length() > 8)
                        return false;
                    return HexFormat.isHexDigit(codePoint) && super.charTyped(codePoint, modifiers);
                }
            };
            this.txtField.setValue(HexFormat.of().toHexDigits(this.color));
            this.txtField.setResponder(s -> {
                if (s.isEmpty())
                    this.color = RenderUtils.DEFAULT_COLOR;
                else
                    this.color = HexFormat.fromHexDigits(s);
                this.update();
            });
            this.addRenderableWidget(this.txtField);
            yOffset += 18;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("tenshilib.patreon.save"), button -> {
                    if (this.effect != null)
                        PatreonClientPlatform.INSTANCE.sendToServer(new C2SEffectUpdatePkt(this.effect.id(), this.render, this.renderLocation, this.color));
                })
                .pos(this.width / 2 - 100, this.height / 8 + yOffset).size(200, 20).build());
        yOffset += 24;
        this.addRenderableWidget(Button.builder(name, button -> this.minecraft.setScreen(this.parent))
                .pos(this.width / 2 - 100, this.height / 8 + yOffset).size(200, 20).build());
    }

    private void updateColorString() {
        this.color = new Color(this.red.getColor().add(this.green.getColor()).add(this.blue.getColor()), this.alpha.getColor().getAlpha()).hex();
        this.update();
        this.txtField.setValue(HexFormat.of().toHexDigits(this.color));
    }

    private void update() {
        this.setting.update(this.effect, this.renderLocation, this.render, this.color);
    }

    private int setLocationButton() {
        if (this.effect == null || !this.effect.guiElements().contains(GuiElement.LOCATION))
            return 0;
        List<RenderLocation> allowed = new ArrayList<>();
        for (RenderLocation l : RenderLocation.values())
            if (this.effect == null || this.effect.locationAllowed(l))
                allowed.add(l);
        if (allowed.isEmpty())
            return 0;
        Function<RenderLocation, Component> f = loc -> Component.translatable("tenshilib.patreon.location." + loc.toString());
        this.addRenderableWidget(this.locationButton = CycleButton.builder(f).withValues(allowed)
                .withInitialValue(this.renderLocation)
                .create(this.width / 2 - 125, this.height / 8 + 24 * 2, 250, 20,
                        Component.translatable("tenshilib.patreon.location"), (cycleButton, loc) -> {
                            this.renderLocation = loc;
                            this.update();
                        }));
        return 24;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        if (this.tier == 0) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("tenshilib.patreon.not").withStyle(ChatFormatting.DARK_RED), this.width / 2, 46, 0xFFFFFF);
        }
        if (this.tier > 0) {
            int scale = 65;
            int sizeX = 100 * scale / 30;
            int sizeY = 100 * scale / 30;
            int ex = this.width / 2 - 180 - sizeX / 2;
            int ey = this.height / 8 + 24 * 5 - sizeY / 2;
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, ex, ey, ex + sizeX, ey + sizeY, scale, 0.0625F, mouseX, mouseY + 0.625F * scale, this.minecraft.player);
            if (this.effect != null && this.effect.guiElements().contains(GuiElement.COLOR))
                guiGraphics.drawCenteredString(this.font, Component.translatable("tenshilib.patreon.color"), this.width / 2 - 55, this.height / 8 + 24 * 6 + 8, 0xFFFFFF);
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (Minecraft.getInstance().getConnection() != null)
            PatreonClientPlatform.INSTANCE.sendToServer(C2SRequestUpdateClientPkt.INSTANCE);
    }
}

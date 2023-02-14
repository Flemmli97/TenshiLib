package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.client.Color;
import io.github.flemmli97.tenshilib.client.render.RenderUtils;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonEffects;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SRequestUpdateClientPkt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Function;

public class PatreonGui extends Screen {

    public static final int BLACK = 0xFF000000;

    private final Screen parent;

    private PatreonEffects.PatreonEffectConfig effect;
    private RenderLocation renderLocation;
    private boolean render = true;
    private int color = RenderUtils.defaultColor;

    private EditBox txtField;
    private HorizontalColorSlider red, green, blue, alpha;

    private int tier;

    private CycleButton<RenderLocation> locationButton;
    private PatreonPlayerSetting setting;

    public PatreonGui(Screen screen) {
        super(new TranslatableComponent("tenshilib.patreon.title").withStyle(ChatFormatting.GOLD));
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
                name = new TranslatableComponent("tenshilib.patreon.level.no");
            else
                name = new TranslatableComponent("tenshilib.patreon.back");
        } else {
            name = CommonComponents.GUI_DONE;
            this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 8 + 24 * 7, 200, 20, new TranslatableComponent("tenshilib.patreon.save"), button -> PatreonClientPlatform.INSTANCE.sendToServer(new C2SEffectUpdatePkt(this.effect.id(), this.render, this.renderLocation, this.color))));
        }
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 8 + 24 * 8, 200, 20, name, button -> this.minecraft.setScreen(this.parent)));
        if (this.tier < 1)
            return;
        this.setting = PatreonPlatform.INSTANCE.playerSettings(this.minecraft.player).orElse(null);
        if (this.setting == null)
            return;
        this.effect = this.setting.effect();
        if (this.effect == null)
            this.effect = PatreonEffects.get(PatreonDataManager.get(this.minecraft.player.getUUID().toString()).defaultEffect());
        this.renderLocation = this.setting.getRenderLocation();
        if (this.effect != null && !this.effect.locationAllowed(this.renderLocation))
            this.renderLocation = this.effect.defaultLoc();
        this.render = this.setting.shouldRender();
        this.color = this.setting.getColor();

        Function<PatreonEffects.PatreonEffectConfig, Component> idF = eff -> new TranslatableComponent("tenshilib.patreon.id." + eff.id());
        List<PatreonEffects.PatreonEffectConfig> effects = new ArrayList<>();
        for (PatreonEffects.PatreonEffectConfig eff : PatreonEffects.allEffects())
            if (eff.tier <= this.tier)
                effects.add(eff);
        this.addRenderableWidget(CycleButton.builder(idF).withValues(effects)
                .withInitialValue(this.effect)
                .create(this.width / 2 - 125, this.height / 8, 250, 20,
                        new TranslatableComponent("tenshilib.patreon.id"), (cycleButton, eff) -> {
                            this.effect = eff;
                            if (!this.effect.locationAllowed(this.renderLocation)) {
                                this.renderLocation = this.effect.defaultLoc();
                            }
                            this.update();
                            this.setLocationButton();
                        }));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.render).create(this.width / 2 - 125, this.height / 8 + 24, 250, 20,
                new TranslatableComponent("tenshilib.patreon.render"), (cycleButton, boolean_) -> {
                    this.render = boolean_;
                    this.update();
                }));
        this.setLocationButton();

        this.addRenderableWidget(this.red = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + 24 * 3, 250, 14,
                new Color(0, true), new Color(255, 0, 0), slider -> this.updateColorString(),
                new TranslatableComponent("tenshilib.patreon.slider.red")));
        this.red.with(this.color);
        this.addRenderableWidget(this.green = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + 24 * 3 + 18, 250, 14,
                new Color(0, true), new Color(0, 255, 0), slider -> this.updateColorString(),
                new TranslatableComponent("tenshilib.patreon.slider.green")));
        this.green.with(this.color);
        this.addRenderableWidget(this.blue = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + 24 * 3 + 18 * 2, 250, 14,
                new Color(0, true), new Color(0, 0, 255), slider -> this.updateColorString(),
                new TranslatableComponent("tenshilib.patreon.slider.blue")));
        this.blue.with(this.color);
        this.addRenderableWidget(this.alpha = new HorizontalColorSlider(this.width / 2 - 125, this.height / 8 + 24 * 3 + 18 * 3, 250, 14,
                new Color(0, false), new Color(255, 255, 255), slider -> this.updateColorString(),
                new TranslatableComponent("tenshilib.patreon.slider.alpha")));
        this.alpha.with(this.color);
        this.txtField = new EditBox(this.minecraft.font, this.width / 2 - 155 + 160, this.height / 8 + 24 * 6, 150, 20, new TranslatableComponent("tenshilib.patreon.color")) {
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
                this.color = RenderUtils.defaultColor;
            else
                this.color = HexFormat.fromHexDigits(s);
            this.update();
        });
        this.addRenderableWidget(this.txtField);
    }

    private void updateColorString() {
        this.color = new Color(this.red.getColor().add(this.green.getColor()).add(this.blue.getColor()), this.alpha.getColor().getAlpha()).hex();
        this.update();
        this.txtField.setValue(HexFormat.of().toHexDigits(this.color));
    }

    private void update() {
        this.setting.update(this.effect, this.renderLocation, this.render, this.color);
    }

    private void setLocationButton() {
        if (this.locationButton != null)
            this.removeWidget(this.locationButton);
        List<RenderLocation> allowed = new ArrayList<>();
        for (RenderLocation l : RenderLocation.values())
            if (this.effect == null || this.effect.locationAllowed(l))
                allowed.add(l);
        Function<RenderLocation, Component> f = loc -> new TranslatableComponent("tenshilib.patreon.location." + loc.toString());
        this.addRenderableWidget(this.locationButton = CycleButton.builder(f).withValues(allowed)
                .withInitialValue(this.renderLocation)
                .create(this.width / 2 - 125, this.height / 8 + 24 * 2, 250, 20,
                        new TranslatableComponent("tenshilib.patreon.location"), (cycleButton, loc) -> {
                            this.renderLocation = loc;
                            this.update();
                        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        GuiComponent.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        if (this.tier == 0) {
            GuiComponent.drawCenteredString(poseStack, this.font, new TranslatableComponent("tenshilib.patreon.not").withStyle(ChatFormatting.DARK_RED), this.width / 2, 46, 0xFFFFFF);
        }
        if (this.tier > 0) {
            int ex = this.width / 2 - 180;
            int ey = this.height / 8 + 24 * 7;
            InventoryScreen.renderEntityInInventory(ex, ey, 65, ex - mouseX, ey - 83 - mouseY, this.minecraft.player);
            GuiComponent.drawCenteredString(poseStack, this.font, new TranslatableComponent("tenshilib.patreon.color"), this.width / 2 - 55, this.height / 8 + 24 * 6 + 8, 0xFFFFFF);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        super.removed();
        PatreonClientPlatform.INSTANCE.sendToServer(new C2SRequestUpdateClientPkt());
    }
}

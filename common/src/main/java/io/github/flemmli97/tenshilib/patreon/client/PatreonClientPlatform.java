package io.github.flemmli97.tenshilib.patreon.client;

import io.github.flemmli97.tenshilib.mixinhelper.ScreenWidgetAdder;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import io.github.flemmli97.tenshilib.platform.InitUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface PatreonClientPlatform {

    PatreonClientPlatform INSTANCE = InitUtil.getPlatformInstance(PatreonClientPlatform.class,
            "io.github.flemmli97.tenshilib.fabric.platform.patreon.ClientPatreonImpl",
            "io.github.flemmli97.tenshilib.forge.platform.patreon.ClientPatreonImpl");

    static void addPatreonButton(Screen screen) {
        if (screen instanceof SkinCustomizationScreen skin) {
            ((ScreenWidgetAdder) screen).widgetAdder(new PatreonButton(screen.width - 32, screen.height - 32, skin));
        }
    }

    static boolean dontRenderArmor(LivingEntity entity, EquipmentSlot equipmentSlot) {
        if (entity instanceof Player player && equipmentSlot == EquipmentSlot.HEAD) {
            return PatreonPlatform.INSTANCE.playerSettings(player).map(setting ->
                    setting.effect() != null && setting.shouldRender() && setting.getRenderLocation() == RenderLocation.HATNOARMOR).orElse(false);
        }
        return false;
    }

    void setup();

    void sendToServer(C2SEffectUpdatePkt pkt);
}

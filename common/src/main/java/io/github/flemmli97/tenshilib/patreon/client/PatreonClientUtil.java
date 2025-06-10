package io.github.flemmli97.tenshilib.patreon.client;

import io.github.flemmli97.tenshilib.mixin.ScreenAccessor;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.TenshiLibPatreonPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PatreonClientUtil {

    private static PatreonButton last;

    public static void addPatreonButton(Screen screen) {
        if (screen instanceof SkinCustomizationScreen skin) {
            if (last != null)
                ((ScreenAccessor) screen).removeWidgetFrom(last);
            ((ScreenAccessor) screen).addRenderableWidgetTo(last = new PatreonButton(skin.width - 32, skin.height - 32, skin));
        } else {
            last = null;
        }
    }

    public static boolean dontRenderArmor(LivingEntity entity, EquipmentSlot equipmentSlot) {
        if (entity instanceof Player player && equipmentSlot == EquipmentSlot.HEAD) {
            PatreonPlayerSetting setting = TenshiLibPatreonPlatform.INSTANCE.playerSettings(player);
            return setting.effect() != null && setting.shouldRender() && setting.getRenderLocation() == RenderLocation.HATNOARMOR;
        }
        return false;
    }
}

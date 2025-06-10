package io.github.flemmli97.tenshilib.fabric.loader.patreon;

import io.github.flemmli97.tenshilib.fabric.mixin.LivingEntityRendererAccessor;
import io.github.flemmli97.tenshilib.patreon.client.PatreonClientUtil;
import io.github.flemmli97.tenshilib.patreon.client.PatreonLayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class FabricPatreonClientUtil {

    public static void setup() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> PatreonClientUtil.addPatreonButton(screen));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addPatreonLayer(Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers) {
        LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>> r = (LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>) playerRenderers.get(PlayerSkin.Model.WIDE);
        ((LivingEntityRendererAccessor) r).add(new PatreonLayer(r));
        r = (LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>) playerRenderers.get(PlayerSkin.Model.SLIM);
        ((LivingEntityRendererAccessor) r).add(new PatreonLayer(r));
    }
}

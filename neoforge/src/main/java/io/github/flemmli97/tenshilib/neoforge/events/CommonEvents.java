package io.github.flemmli97.tenshilib.neoforge.events;

import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.entity.data.SyncedMobDataHandler;
import io.github.flemmli97.tenshilib.common.item.DualWeapon;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class CommonEvents {

    public static void disableOffhand(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getEntity().getMainHandItem().getItem() instanceof DualWeapon weapon && weapon.disableOffhand())
            event.setCanceled(true);
    }

    public static void disableOffhandBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getEntity().getMainHandItem().getItem() instanceof DualWeapon weapon && weapon.disableOffhand()) {
            event.setUseItem(TriState.FALSE);
        }
    }

    public static void onTracking(PlayerEvent.StartTracking event) {
        AnimationState anim;
        if (event.getTarget() instanceof AnimatedEntity animated && (anim = animated.getAnimationHandler().getAnimation()) != null) {
            LoaderNetwork.INSTANCE.sendToPlayer(S2CEntityAnimation.create((Entity & AnimatedEntity) event.getTarget(),
                    anim.getStartTransition(), anim.getEndTransitionTime(), anim.getTick(1)), (ServerPlayer) event.getEntity());
        }
        if (event.getTarget() instanceof SyncedMobDataHandler handler) {
            handler.getDataContainer().sendEntriesTo((ServerPlayer) event.getEntity());
        }
    }
}

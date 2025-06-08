package io.github.flemmli97.tenshilib.fabric;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.fabric.events.CommonEvents;
import io.github.flemmli97.tenshilib.fabric.network.PacketHandler;
import io.github.flemmli97.tenshilib.fabric.platform.patreon.PatreonImpl;
import io.github.flemmli97.tenshilib.platform.NetworkCrossPlat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.DispenserBlock;

public class TenshiLibFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register(CommonEvents::leftClickBlock);
        UseItemCallback.EVENT.register(CommonEvents::disableOffhand);
        EntityTrackingEvents.START_TRACKING.register(((entity, player) -> {
            if (entity instanceof IAnimated animated && animated.getAnimationHandler().hasAnimation()) {
                AnimatedAction anim = animated.getAnimationHandler().getAnimation();
                NetworkCrossPlat.INSTANCE.sendToClient(S2CEntityAnimation.create((Entity & IAnimated) entity,
                        anim.getStartTransition(), anim.getEndTransitionTime(), anim.getTick(1)), player);
            }
        }));
        PacketHandler.register();
        for (SpawnEgg egg : SpawnEgg.getEggs())
            DispenserBlock.registerBehavior(egg, egg.dispenser());
        PatreonImpl.initPatreonData();
    }
}

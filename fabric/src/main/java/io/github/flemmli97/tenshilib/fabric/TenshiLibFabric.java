package io.github.flemmli97.tenshilib.fabric;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.CustomDataSerializers;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.fabric.events.CommonEvents;
import io.github.flemmli97.tenshilib.fabric.network.ServerPacketHandler;
import io.github.flemmli97.tenshilib.fabric.platform.patreon.PatreonImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.DispenserBlock;

public class TenshiLibFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CustomDataSerializers.register();
        AttackBlockCallback.EVENT.register(CommonEvents::leftClickBlock);
        UseItemCallback.EVENT.register(CommonEvents::disableOffhand);
        EntityTrackingEvents.START_TRACKING.register(((entity, player) -> {
            if (entity instanceof IAnimated animated && animated.getAnimationHandler().hasAnimation()) {
                S2CEntityAnimation pkt = S2CEntityAnimation.create((Entity & IAnimated) entity);
                FriendlyByteBuf buf = PacketByteBufs.create();
                pkt.write(buf);
                ServerPlayNetworking.send(player, pkt.getID(), buf);
            }
        }));
        ServerPacketHandler.register();
        for (SpawnEgg egg : SpawnEgg.getEggs())
            DispenserBlock.registerBehavior(egg, egg.dispenser());
        PatreonImpl.initPatreonData();
    }
}

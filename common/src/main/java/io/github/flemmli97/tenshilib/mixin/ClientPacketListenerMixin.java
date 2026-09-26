package io.github.flemmli97.tenshilib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A fix for MC-273361 mob position being desynced and unable to update on the client due to not in ticking distance.
 * This causes teleporting mobs to be invisible on the client as their position is still their old one (since vanilla lerps it).
 * This is fixed in 1.21.2+ vanilla.
 * Backporting cause few mods of mine have teleporting entities with long distances
 */
@Mixin(value = ClientPacketListener.class, priority = 999)
public class ClientPacketListenerMixin {

    @WrapOperation(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFI)V"))
    private void fixEntityTeleport(Entity instance, double x, double y, double z, float yRot, float xRot, int steps, Operation<Void> original) {
        if (instance.position().distanceToSqr(x, y, z) > 4096) {
            instance.absMoveTo(x, y, z, yRot, xRot);
        } else {
            original.call(instance, x, y, z, yRot, xRot, steps);
        }
    }
}

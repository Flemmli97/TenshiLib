package io.github.flemmli97.tenshilib.fabric.mixin;


import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EntityTrackingEvents.START_TRACKING too early.
 * Need to the client spawn packet sent first
 */
@Mixin(ServerEntity.class)
public abstract class TrackingMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "addPairing", at = @At("RETURN"))
    private void onStartTracking(ServerPlayer player, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayer target)
            PatreonPlatform.INSTANCE.sendToClient(player, target);
    }
}
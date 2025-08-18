package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.common.entity.data.SyncedMobDataHandler;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {

    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "sendChanges", at = @At("RETURN"))
    private void onSendEntityData(CallbackInfo info) {
        if (this.entity instanceof SyncedMobDataHandler handler && handler.getDataContainer().isDirty())
            handler.getDataContainer().sendDirtyEntriesToTracking();
    }
}

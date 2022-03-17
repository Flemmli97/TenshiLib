package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.fabric.platform.patreon.PlayerPatreonData;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void copyOld(ServerPlayer oldPlayer, boolean alive, CallbackInfo info) {
        ((PlayerPatreonData) this).settings().read(((PlayerPatreonData) oldPlayer).settings().save(new CompoundTag()));
        PatreonPlatform.INSTANCE.sendToClient((ServerPlayer) (Object) this, (ServerPlayer) (Object) this);
    }
}

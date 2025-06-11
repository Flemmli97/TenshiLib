package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.common.data.SyncedReloadListeners;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void onPlace(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        SyncedReloadListeners.triggerSync(Collections.singleton(player));
    }

    @Inject(method = "reloadResources", at = @At("RETURN"))
    private void onReloadAll(CallbackInfo info) {
        SyncedReloadListeners.triggerSync(((PlayerList) (Object) this).getPlayers());
    }
}

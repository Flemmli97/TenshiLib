package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.mixinhelper.PlayerAttackAccess;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerAttackAccess {

    @Unique
    private boolean tenshilib$noSweep, tenshilib$noCooldownReset;

    @Inject(method = "resetAttackStrengthTicker", at = @At("HEAD"), cancellable = true)
    private void onReset(CallbackInfo info) {
        if (this.tenshilib$noCooldownReset)
            info.cancel();
    }

    @Override
    public void tenshilib$SetNoStrengthResetState(boolean noReset) {
        this.tenshilib$noCooldownReset = noReset;
    }

    @Override
    public void tenshilib$SetNoSweeping(boolean noSweeping) {
        this.tenshilib$noSweep = noSweeping;
    }

    @Override
    public boolean tenshilib$IsSweepDisabled() {
        return this.tenshilib$noSweep;
    }
}

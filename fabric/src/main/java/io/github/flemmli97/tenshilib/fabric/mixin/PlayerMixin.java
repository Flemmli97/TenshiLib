package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.fabric.platform.patreon.PlayerPatreonData;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerPatreonData {

    @Unique
    private final PatreonPlayerSetting tenshilib_patreon_setting = new PatreonPlayerSetting((Player) (Object) this);

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadData(CompoundTag compound, CallbackInfo info) {
        this.tenshilib_patreon_setting.read(compound.getCompound("TenshiLib:Patreon"));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveData(CompoundTag compound, CallbackInfo info) {
        compound.put("TenshiLib:Patreon", this.tenshilib_patreon_setting.save(new CompoundTag()));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        this.tenshilib_patreon_setting.tick((Player) (Object) this);
    }

    @Override
    public PatreonPlayerSetting settings() {
        return this.tenshilib_patreon_setting;
    }
}

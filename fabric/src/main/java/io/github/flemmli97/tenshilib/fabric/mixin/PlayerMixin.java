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
    private PatreonPlayerSetting setting = new PatreonPlayerSetting((Player) (Object) this);

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadData(CompoundTag compound, CallbackInfo info) {
        this.setting.read(compound.getCompound("TenshiLib:Patreon"));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveData(CompoundTag compound, CallbackInfo info) {
        compound.put("TenshiLib:Patreon", this.setting.save(new CompoundTag()));
    }

    @Override
    public PatreonPlayerSetting settings() {
        return this.setting;
    }
}

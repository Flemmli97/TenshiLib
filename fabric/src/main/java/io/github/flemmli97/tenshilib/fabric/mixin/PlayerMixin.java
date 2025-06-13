package io.github.flemmli97.tenshilib.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.tenshilib.fabric.loader.patreon.PlayerPatreonData;
import io.github.flemmli97.tenshilib.mixinhelper.PlayerAttackAccess;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerPatreonData {

    @Unique
    private final PatreonPlayerSetting tenshilib$patreon_setting = new PatreonPlayerSetting((Player) (Object) this);

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadData(CompoundTag compound, CallbackInfo info) {
        this.tenshilib$patreon_setting.read(compound.getCompound("TenshiLib:Patreon"));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveData(CompoundTag compound, CallbackInfo info) {
        compound.put("TenshiLib:Patreon", this.tenshilib$patreon_setting.save(new CompoundTag()));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        this.tenshilib$patreon_setting.tick((Player) (Object) this);
    }

    @WrapOperation(method = "attack", constant = @Constant(classValue = SwordItem.class))
    private boolean modifySweeping(Object object, Operation<Boolean> original) {
        return !((PlayerAttackAccess) this).tenshilib$IsSweepDisabled() && original.call(object);
    }

    @Override
    public PatreonPlayerSetting tenshilib$Settings() {
        return this.tenshilib$patreon_setting;
    }
}

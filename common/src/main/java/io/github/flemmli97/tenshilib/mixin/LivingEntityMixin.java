package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.common.effect.ExtendedMobEffect;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "onEffectAdded", at = @At("RETURN"))
    private void onAddedEffect(MobEffectInstance instance, Entity entity, CallbackInfo ci) {
        if (instance.getEffect().value() instanceof ExtendedMobEffect ext) {
            ext.onEffectAdded(((LivingEntity) (Object) this), instance);
            if (ext.shouldSync() && !((Object) this instanceof Player) && !((LivingEntity) (Object) this).level().isClientSide) {
                if (ext.shouldSync()) {
                    LoaderNetwork.INSTANCE.sendVanillaToTracking(new ClientboundUpdateMobEffectPacket(((LivingEntity) (Object) this).getId(), instance, true),
                            (LivingEntity) (Object) this);
                }
            }
        }
    }

    @Inject(method = "onEffectUpdated", at = @At("RETURN"))
    private void onUpdatedEffect(MobEffectInstance instance, boolean forced, Entity entity, CallbackInfo ci) {
        if (instance.getEffect().value() instanceof ExtendedMobEffect ext) {
            ext.onEffectUpdated(((LivingEntity) (Object) this), instance);
            if (ext.shouldSync() && !((Object) this instanceof Player) && !((LivingEntity) (Object) this).level().isClientSide) {
                LoaderNetwork.INSTANCE.sendVanillaToTracking(new ClientboundUpdateMobEffectPacket(((LivingEntity) (Object) this).getId(), instance, false),
                        (LivingEntity) (Object) this);
            }
        }
    }

    @Inject(method = "onEffectRemoved", at = @At("RETURN"))
    private void onAddedEffect(MobEffectInstance instance, CallbackInfo ci) {
        if (instance.getEffect().value() instanceof ExtendedMobEffect ext) {
            ext.onEffectRemoved(((LivingEntity) (Object) this), instance);
            if (ext.shouldSync() && !((Object) this instanceof Player) && !((LivingEntity) (Object) this).level().isClientSide) {
                LoaderNetwork.INSTANCE.sendVanillaToTracking(new ClientboundRemoveMobEffectPacket(((LivingEntity) (Object) this).getId(), instance.getEffect()),
                        (LivingEntity) (Object) this);
            }
        }
    }
}

package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.common.effect.SyncedMobEffect;
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

    @Inject(method = "onEffectAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;addAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;I)V"))
    private void onAddedEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
        if (!((Object) this instanceof Player) && effectInstance.getEffect().value() instanceof SyncedMobEffect) {
            LoaderNetwork.INSTANCE.sendVanillaToTracking(new ClientboundUpdateMobEffectPacket(((LivingEntity) (Object) this).getId(), effectInstance, false),
                    (LivingEntity) (Object) this);
        }
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"))
    private void onAddedEffect(MobEffectInstance effectInstance, CallbackInfo ci) {
        if (!((Object) this instanceof Player) && effectInstance.getEffect().value() instanceof SyncedMobEffect) {
            LoaderNetwork.INSTANCE.sendVanillaToTracking(new ClientboundRemoveMobEffectPacket(((LivingEntity) (Object) this).getId(), effectInstance.getEffect()),
                    (LivingEntity) (Object) this);

        }
    }
}

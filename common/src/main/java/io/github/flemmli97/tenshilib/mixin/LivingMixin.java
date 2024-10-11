package io.github.flemmli97.tenshilib.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingMixin {

    @Invoker("getKnockback")
    float getKnockback(Entity attacker, DamageSource damageSource);

}

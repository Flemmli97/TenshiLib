package io.github.flemmli97.tenshilib.common.utils;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class AOEWeaponHandler {

    public static void onAOEWeaponSwing(Player player, ItemStack stack, IAOEWeapon weapon) {
        if (player.level.isClientSide)
            return;
        List<Entity> list = RayTraceUtils.getEntities(player, weapon.getRange(player, stack), weapon.getFOV(player, stack));
        if (EventCalls.INSTANCE.aoeAttackCall(player, stack, list) || list.isEmpty())
            return;
        for (int i = 0; i < list.size(); i++)
            attack(player, list.get(i), i == (list.size() - 1), weapon.doSweepingAttack());
    }

    /**
     * Exact same like in {@link Player#attack(Entity)} but with the option of disabling the cooldown and no sweep attack
     */
    public static void attack(Player player, Entity target, boolean resetCooldown, boolean canDoSweep) {
        if (!EventCalls.INSTANCE.playerAttackCall(player, target)) return;
        if (target.isAttackable()) {
            if (!target.skipAttackInteraction(player)) {
                float baseDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float enchantBonus;
                if (target instanceof LivingEntity) {
                    enchantBonus = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), ((LivingEntity) target).getMobType());
                } else {
                    enchantBonus = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
                }

                float cooldown = player.getAttackStrengthScale(0.5F);
                baseDmg = baseDmg * (0.2F + cooldown * cooldown * 0.8F);
                enchantBonus = enchantBonus * cooldown;
                if (resetCooldown)
                    player.resetAttackStrengthTicker();
                if (baseDmg > 0.0F || enchantBonus > 0.0F) {
                    boolean flag = cooldown > 0.9F;
                    int knocback = EnchantmentHelper.getKnockbackBonus(player);
                    if (player.isSprinting() && flag) {
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
                        ++knocback;
                    }

                    boolean crit = flag && player.fallDistance > 0.0F && !player.isOnGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && target instanceof LivingEntity && !player.isSprinting();
                    Pair<Boolean, Float> critCall = EventCalls.INSTANCE.criticalAttackCall(player, target, crit, crit ? 1.5F : 1.0F);
                    crit = critCall.getKey();
                    if (crit) {
                        baseDmg *= critCall.getRight();
                    }
                    baseDmg = baseDmg + enchantBonus;
                    float targetHealth = 0.0F;
                    boolean burn = false;
                    int j = EnchantmentHelper.getFireAspect(player);
                    if (target instanceof LivingEntity) {
                        targetHealth = ((LivingEntity) target).getHealth();
                        if (j > 0 && !target.isOnFire()) {
                            burn = true;
                            target.setSecondsOnFire(1);
                        }
                    }
                    Vec3 vector3d = target.getDeltaMovement();
                    boolean attackSuccess = target.hurt(DamageSource.playerAttack(player), baseDmg);
                    if (attackSuccess) {
                        if (knocback > 0) {
                            if (target instanceof LivingEntity) {
                                ((LivingEntity) target).knockback((float) knocback * 0.5F, Mth.sin(player.getYRot() * ((float) Math.PI / 180F)), (-Mth.cos(player.getYRot() * ((float) Math.PI / 180F))));
                            } else {
                                target.push((-Mth.sin(player.getYRot() * ((float) Math.PI / 180F)) * (float) knocback * 0.5F), 0.1D, (Mth.cos(player.getYRot() * ((float) Math.PI / 180F)) * (float) knocback * 0.5F));
                            }

                            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            player.setSprinting(false);
                        }

                        if (cooldown > 0.9f && resetCooldown && canDoSweep) {
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                            player.sweepAttack();
                        }

                        if (target instanceof ServerPlayer && target.hurtMarked) {
                            ((ServerPlayer) target).connection.send(new ClientboundSetEntityMotionPacket(target));
                            target.hurtMarked = false;
                            target.setDeltaMovement(vector3d);
                        }

                        if (crit) {
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                            player.crit(target);
                        }

                        if (!crit) {
                            if (flag) {
                                player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
                            } else {
                                player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, player.getSoundSource(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantBonus > 0.0F) {
                            player.magicCrit(target);
                        }

                        player.setLastHurtMob(target);
                        if (target instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity) target, player);
                        }

                        EnchantmentHelper.doPostDamageEffects(player, target);
                        ItemStack itemstack1 = player.getMainHandItem();
                        Entity entity = target;
                        if (target instanceof EnderDragonPart) {
                            entity = ((EnderDragonPart) target).parentMob;
                        }

                        if (!player.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = itemstack1.copy();
                            itemstack1.hurtEnemy((LivingEntity) entity, player);
                            if (itemstack1.isEmpty()) {
                                EventCalls.INSTANCE.destroyItemCall(player, copy, InteractionHand.MAIN_HAND);
                                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (target instanceof LivingEntity) {
                            float damage = targetHealth - ((LivingEntity) target).getHealth();
                            player.awardStat(Stats.DAMAGE_DEALT, Math.round(damage * 10.0F));
                            if (j > 0) {
                                target.setSecondsOnFire(j * 4);
                            }

                            if (player.level instanceof ServerLevel && damage > 2.0F) {
                                int k = (int) (damage * 0.5D);
                                ((ServerLevel) player.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.causeFoodExhaustion(0.1F);
                    } else {
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
                        if (burn) {
                            target.clearFire();
                        }
                    }
                }

            }
        }
    }
}

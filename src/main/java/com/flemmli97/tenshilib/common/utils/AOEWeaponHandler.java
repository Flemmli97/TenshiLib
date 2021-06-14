package com.flemmli97.tenshilib.common.utils;

import com.flemmli97.tenshilib.api.event.AOEAttackEvent;
import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import java.util.List;

public class AOEWeaponHandler {

    public static void onAOEWeaponSwing(PlayerEntity player, IAOEWeapon weapon) {
        List<Entity> list = RayTraceUtils.getEntities(player, weapon.getRange(), weapon.getFOV());
        if (MinecraftForge.EVENT_BUS.post(new AOEAttackEvent(player, list)) || list.isEmpty())
            return;
        for (int i = 0; i < list.size(); i++)
            attack(player, list.get(i), i == (list.size() - 1), weapon.doSweepingAttack());
    }

    /**
     * Exact same like in {@link PlayerEntity#attackTargetEntityWithCurrentItem(Entity)} but with the option of disabling the cooldown and no sweep attack
     */
    public static void attack(PlayerEntity player, Entity target, boolean resetCooldown, boolean canDoSweep) {
        if (!ForgeHooks.onPlayerAttackTarget(player, target)) return;
        if (target.canBeAttackedWithItem()) {
            if (!target.hitByEntity(player)) {
                float baseDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float enchantBonus;
                if (target instanceof LivingEntity) {
                    enchantBonus = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((LivingEntity) target).getCreatureAttribute());
                } else {
                    enchantBonus = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                }

                float cooldown = player.getCooledAttackStrength(0.5F);
                baseDmg = baseDmg * (0.2F + cooldown * cooldown * 0.8F);
                enchantBonus = enchantBonus * cooldown;
                if (resetCooldown)
                    player.resetCooldown();
                if (baseDmg > 0.0F || enchantBonus > 0.0F) {
                    boolean flag = cooldown > 0.9F;
                    int knocback = EnchantmentHelper.getKnockbackModifier(player);
                    if (player.isSprinting() && flag) {
                        player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        ++knocback;
                    }

                    boolean crit = flag && player.fallDistance > 0.0F && !player.isOnGround() && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && target instanceof LivingEntity && !player.isSprinting();
                    CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, target, crit, crit ? 1.5F : 1.0F);
                    crit = hitResult != null;
                    if (crit) {
                        baseDmg *= hitResult.getDamageModifier();
                    }
                    baseDmg = baseDmg + enchantBonus;
                    float targetHealth = 0.0F;
                    boolean burn = false;
                    int j = EnchantmentHelper.getFireAspectModifier(player);
                    if (target instanceof LivingEntity) {
                        targetHealth = ((LivingEntity) target).getHealth();
                        if (j > 0 && !target.isBurning()) {
                            burn = true;
                            target.setFire(1);
                        }
                    }
                    Vector3d vector3d = target.getMotion();
                    boolean attackSuccess = target.attackEntityFrom(DamageSource.causePlayerDamage(player), baseDmg);
                    if (attackSuccess) {
                        if (knocback > 0) {
                            if (target instanceof LivingEntity) {
                                ((LivingEntity) target).applyKnockback((float) knocback * 0.5F, MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), (-MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F))));
                            } else {
                                target.addVelocity((-MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knocback * 0.5F), 0.1D, (MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knocback * 0.5F));
                            }

                            player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
                            player.setSprinting(false);
                        }

                        if (cooldown > 0.9f && resetCooldown && canDoSweep) {
                            player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                            player.spawnSweepParticles();
                        }

                        if (target instanceof ServerPlayerEntity && target.velocityChanged) {
                            ((ServerPlayerEntity) target).connection.sendPacket(new SEntityVelocityPacket(target));
                            target.velocityChanged = false;
                            target.setMotion(vector3d);
                        }

                        if (crit) {
                            player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(target);
                        }

                        if (!crit) {
                            if (flag) {
                                player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantBonus > 0.0F) {
                            player.onEnchantmentCritical(target);
                        }

                        player.setLastAttackedEntity(target);
                        if (target instanceof LivingEntity) {
                            EnchantmentHelper.applyThornEnchantments((LivingEntity) target, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, target);
                        ItemStack itemstack1 = player.getHeldItemMainhand();
                        Entity entity = target;
                        if (target instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) target).dragon;
                        }

                        if (!player.world.isRemote && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = itemstack1.copy();
                            itemstack1.hitEntity((LivingEntity) entity, player);
                            if (itemstack1.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(player, copy, Hand.MAIN_HAND);
                                player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (target instanceof LivingEntity) {
                            float damage = targetHealth - ((LivingEntity) target).getHealth();
                            player.addStat(Stats.DAMAGE_DEALT, Math.round(damage * 10.0F));
                            if (j > 0) {
                                target.setFire(j * 4);
                            }

                            if (player.world instanceof ServerWorld && damage > 2.0F) {
                                int k = (int) (damage * 0.5D);
                                ((ServerWorld) player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, target.getPosX(), target.getPosYHeight(0.5D), target.getPosZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.1F);
                    } else {
                        player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                        if (burn) {
                            target.extinguish();
                        }
                    }
                }

            }
        }
    }
}

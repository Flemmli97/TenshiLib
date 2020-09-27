package com.flemmli97.tenshilib.common.network;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPacketHit {

    private final HitType type;

    public C2SPacketHit(HitType type) {
        this.type = type;
    }

    public static C2SPacketHit fromBytes(PacketBuffer buf) {
        return new C2SPacketHit(HitType.values()[buf.readInt()]);
    }

    public static void toBytes(C2SPacketHit pkt, PacketBuffer buf) {
        buf.writeInt(pkt.type.ordinal());
    }

    public static void handlePacket(C2SPacketHit pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()-> {
            PlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;
            ItemStack stack = player.getHeldItemMainhand();
        /*if(pkt.type == HitType.EXT && stack.getItem() instanceof IExtendedWeapon){
            IExtendedWeapon item = (IExtendedWeapon) stack.getItem();
            RayTraceResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange());
            if(res != null && res.entityHit != null)
                player.attackTargetEntityWithCurrentItem(res.entityHit);
        }
        if(pkt.type == HitType.AOE && stack.getItem() instanceof IAOEWeapon){
            IAOEWeapon item = (IAOEWeapon) stack.getItem();
            List<EntityLivingBase> list = RayTraceUtils.getEntities(player, item.getRange(), item.getFOV());
            if(MinecraftForge.EVENT_BUS.post(new AOEAttackEvent(player, list)) || list.isEmpty())
                return;
            for(int i = 0; i < list.size(); i++)
                attackTargetEntityWithCurrentItem(player, list.get(i), i == (list.size() - 1));
        }*/
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Exact same like in {@link PlayerEntity#attackTargetEntityWithCurrentItem(Entity)} but with the option of disabling the cooldown and no sweep attack
     */
    public static void attackWithoutSweep(PlayerEntity player, Entity target, boolean resetCooldown) {
        if (!ForgeHooks.onPlayerAttackTarget(player, target)) return;
        if (target.canBeAttackedWithItem()) {
            if (!target.hitByEntity(player)) {
                float f = (float)player.getAttributeValue(Attributes.GENERIC_ATTACK_DAMAGE);
                float f1;
                if (target instanceof LivingEntity) {
                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((LivingEntity)target).getCreatureAttribute());
                } else {
                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                }

                float f2 = player.getCooledAttackStrength(0.5F);
                f = f * (0.2F + f2 * f2 * 0.8F);
                f1 = f1 * f2;
                if(resetCooldown)
                    player.resetCooldown();
                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = f2 > 0.9F;
                    int i = 0;
                    i = i + EnchantmentHelper.getKnockbackModifier(player);
                    if (player.isSprinting() && flag) {
                        player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        ++i;
                    }

                    boolean flag2 = flag && player.fallDistance > 0.0F && !player.isOnGround() && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && target instanceof LivingEntity;
                    flag2 = flag2 && !player.isSprinting();
                    CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, target, flag2, flag2 ? 1.5F : 1.0F);
                    flag2 = hitResult != null;
                    if (flag2) {
                        f *= hitResult.getDamageModifier();
                    }
                    f = f + f1;
                    double d0 = (player.distanceWalkedModified - player.prevDistanceWalkedModified);
                    float f4 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(player);
                    if (target instanceof LivingEntity) {
                        f4 = ((LivingEntity)target).getHealth();
                        if (j > 0 && !target.isBurning()) {
                            flag4 = true;
                            target.setFire(1);
                        }
                    }
                    Vector3d vector3d = target.getMotion();
                    boolean flag5 = target.attackEntityFrom(DamageSource.causePlayerDamage(player), f);
                    if (flag5) {
                        if (i > 0) {
                            if (target instanceof LivingEntity) {
                                ((LivingEntity)target).takeKnockback((float)i * 0.5F, MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))));
                            } else {
                                target.addVelocity((-MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)) * (float)i * 0.5F), 0.1D, (MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)) * (float)i * 0.5F));
                            }

                            player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
                            player.setSprinting(false);
                        }

                        if (target instanceof ServerPlayerEntity && target.velocityChanged) {
                            ((ServerPlayerEntity)target).connection.sendPacket(new SEntityVelocityPacket(target));
                            target.velocityChanged = false;
                            target.setMotion(vector3d);
                        }

                        if (flag2) {
                            player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(target);
                        }

                        if (!flag2) {
                            if (flag) {
                                player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            player.onEnchantmentCritical(target);
                        }

                        player.setLastAttackedEntity(target);
                        if (target instanceof LivingEntity) {
                            EnchantmentHelper.applyThornEnchantments((LivingEntity)target, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, target);
                        ItemStack itemstack1 = player.getHeldItemMainhand();
                        Entity entity = target;
                        if (target instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity)target).dragon;
                        }

                        if (!player.world.isRemote && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = itemstack1.copy();
                            itemstack1.hitEntity((LivingEntity)entity, player);
                            if (itemstack1.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(player, copy, Hand.MAIN_HAND);
                                player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (target instanceof LivingEntity) {
                            float f5 = f4 - ((LivingEntity)target).getHealth();
                            player.addStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            if (j > 0) {
                                target.setFire(j * 4);
                            }

                            if (player.world instanceof ServerWorld && f5 > 2.0F) {
                                int k = (int)(f5 * 0.5D);
                                ((ServerWorld)player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5D), target.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.1F);
                    } else {
                        player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                        if (flag4) {
                            target.extinguish();
                        }
                    }
                }

            }
        }
    }
    public enum HitType {
        EXT, AOE
    }
}

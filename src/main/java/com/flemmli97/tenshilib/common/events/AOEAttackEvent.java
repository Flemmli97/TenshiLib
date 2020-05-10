package com.flemmli97.tenshilib.common.events;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.List;

@Cancelable
public class AOEAttackEvent extends PlayerEvent {

    private List<EntityLivingBase> list;

    public AOEAttackEvent(EntityPlayer player, List<EntityLivingBase> attackList) {
        super(player);
        this.list = attackList;
    }

    public List<EntityLivingBase> attackList() {
        return this.list;
    }

    /**
     * Exact same like in {@link EntityPlayer#attackTargetEntityWithCurrentItem(Entity)} but with the option of disabling the cooldown and no sweep attack
     * @param player
     * @param targetEntity
     * @param resetCooldown
     */
    public static void attackTargetEntityWithCurrentItem(EntityPlayer player, Entity targetEntity, boolean resetCooldown) {
        if(!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(player, targetEntity))
            return;
        if(targetEntity.canBeAttackedWithItem()){
            if(!targetEntity.hitByEntity(player)){
                float f = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float f1;

                if(targetEntity instanceof EntityLivingBase){
                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(),
                            ((EntityLivingBase) targetEntity).getCreatureAttribute());
                }else{
                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                }

                float f2 = player.getCooledAttackStrength(0.5F);
                f = f * (0.2F + f2 * f2 * 0.8F);
                f1 = f1 * f2;
                if(resetCooldown)
                    player.resetCooldown();

                if(f > 0.0F || f1 > 0.0F){
                    boolean flag = f2 > 0.9F;
                    int i = 0;
                    i = i + EnchantmentHelper.getKnockbackModifier(player);

                    if(player.isSprinting() && flag){
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
                                player.getSoundCategory(), 1.0F, 1.0F);
                        ++i;
                    }

                    boolean flag2 = flag && player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater()
                            && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding() && targetEntity instanceof EntityLivingBase;
                    flag2 = flag2 && !player.isSprinting();

                    net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(player,
                            targetEntity, flag2, flag2 ? 1.5F : 1.0F);
                    flag2 = hitResult != null;
                    if(flag2){
                        f *= hitResult.getDamageModifier();
                    }

                    f = f + f1;

                    float f4 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(player);

                    if(targetEntity instanceof EntityLivingBase){
                        f4 = ((EntityLivingBase) targetEntity).getHealth();

                        if(j > 0 && !targetEntity.isBurning()){
                            flag4 = true;
                            targetEntity.setFire(1);
                        }
                    }

                    double d1 = targetEntity.motionX;
                    double d2 = targetEntity.motionY;
                    double d3 = targetEntity.motionZ;
                    boolean flag5 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), f);

                    if(flag5){
                        if(i > 0){
                            if(targetEntity instanceof EntityLivingBase){
                                ((EntityLivingBase) targetEntity).knockBack(player, (float) i * 0.5F,
                                        MathHelper.sin(player.rotationYaw * 0.017453292F),
                                        -MathHelper.cos(player.rotationYaw * 0.017453292F));
                            }else{
                                targetEntity.addVelocity(-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) i * 0.5F, 0.1D,
                                        MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) i * 0.5F);
                            }

                            player.motionX *= 0.6D;
                            player.motionZ *= 0.6D;
                            player.setSprinting(false);
                        }

                        if(targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged){
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = d1;
                            targetEntity.motionY = d2;
                            targetEntity.motionZ = d3;
                        }

                        if(flag2){
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                                    player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(targetEntity);
                        }

                        if(!flag2){
                            if(flag){
                                player.world.playSound(null, player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            }else{
                                player.world.playSound(null, player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if(f1 > 0.0F){
                            player.onEnchantmentCritical(targetEntity);
                        }

                        player.setLastAttackedEntity(targetEntity);

                        if(targetEntity instanceof EntityLivingBase){
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
                        ItemStack itemstack1 = player.getHeldItemMainhand();
                        Entity entity = targetEntity;

                        if(targetEntity instanceof MultiPartEntityPart){
                            IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;

                            if(ientitymultipart instanceof EntityLivingBase){
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if(!itemstack1.isEmpty() && entity instanceof EntityLivingBase){
                            ItemStack beforeHitCopy = itemstack1.copy();
                            itemstack1.hitEntity((EntityLivingBase) entity, player);

                            if(itemstack1.isEmpty()){
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
                                player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if(targetEntity instanceof EntityLivingBase){
                            float f5 = f4 - ((EntityLivingBase) targetEntity).getHealth();
                            player.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if(j > 0){
                                targetEntity.setFire(j * 4);
                            }

                            if(player.world instanceof WorldServer && f5 > 2.0F){
                                int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX,
                                        targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.1F);
                    }else{
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
                                player.getSoundCategory(), 1.0F, 1.0F);

                        if(flag4){
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }

}

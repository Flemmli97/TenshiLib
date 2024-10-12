package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {

    public static boolean isItemBetter(LivingEntity holder, @Nullable LivingEntity target, ItemStack stack, ItemStack currentEquipped) {
        if (target == null) {
            target = holder instanceof Mob mob && mob.getTarget() != null ? mob.getTarget() : holder;
        }
        if (stack.getItem() instanceof ArmorItem) {
            if (!(currentEquipped.getItem() instanceof ArmorItem) || EnchantmentHelper.has(currentEquipped, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
                return true;
            else if (currentEquipped.getItem() instanceof ArmorItem itemarmor1) {
                ArmorItem itemarmor = (ArmorItem) stack.getItem();

                if (itemarmor.getDefense() == itemarmor1.getDefense()) {
                    return stack.getDamageValue() > currentEquipped.getDamageValue() || stack.getComponentsPatch().isEmpty() && !currentEquipped.getComponentsPatch().isEmpty();
                } else {
                    return itemarmor.getDefense() > itemarmor1.getDefense();
                }
            }
        }
        if (currentEquipped.isEmpty())
            return true;
        double d1 = damage(holder, target, stack);
        double d2 = damage(holder, target, currentEquipped);
        return d1 > d2;
    }

    public static double damageRaw(ItemStack stack) {
        AttributeInstance m = new AttributeInstance(Attributes.ATTACK_DAMAGE, (inst) -> {
        });
        ItemAttributeModifiers stackMod = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (stackMod != null)
            stackMod.forEach(EquipmentSlot.MAINHAND, (attr, mod) -> {
                if (attr.equals(Attributes.ATTACK_DAMAGE))
                    m.addTransientModifier(mod);
            });
        return Attributes.ATTACK_DAMAGE.value().sanitizeValue(m.getValue());
    }

    public static double damage(LivingEntity holder, @Nullable LivingEntity target, ItemStack stack) {
        double dmg = damageRaw(stack);
        DamageSource damageSource = holder.damageSources().mobAttack(holder);
        if (stack.getItem() instanceof BowItem)
            damageSource = holder.damageSources().arrow(EntityType.ARROW.create(holder.level()), holder);
        double bonus = holder.level() instanceof ServerLevel serverLevel ?
                EnchantmentHelper.modifyDamage(serverLevel, holder.getWeaponItem(), target == null ? holder : target, damageSource, (float) dmg) - dmg
                : 0;
        return dmg + bonus;
    }

    /**
     * Tests, if the players inventory has enough space for the itemstack without actually adding it to the inventory
     */
    public static boolean hasSpace(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Inventory inv = player.getInventory();
        stack = stack.copy();
        for (ItemStack invStack : inv.items) {
            if (invStack.isEmpty()) {
                stack.setCount(stack.getCount() - stack.getMaxStackSize());
            } else if (invStack.getCount() < invStack.getMaxStackSize() && ItemStack.matches(stack, invStack)) {
                int sub = invStack.getMaxStackSize() - invStack.getCount();
                stack.setCount(stack.getCount() - sub);
            }
            if (stack.getCount() <= 0) {
                break;
            }
        }
        return stack.getCount() <= 0;
    }

}

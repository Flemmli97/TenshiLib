package com.flemmli97.tenshilib.common.utils;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

import java.util.Collection;

public class ItemUtils {

    public static boolean isItemBetter(ItemStack stack, ItemStack currentEquipped) {
        if (stack.getItem() instanceof ArmorItem) {
            if (!(currentEquipped.getItem() instanceof ArmorItem) || EnchantmentHelper.hasBindingCurse(currentEquipped))
                return true;
            else if (currentEquipped.getItem() instanceof ArmorItem) {
                ArmorItem itemarmor = (ArmorItem) stack.getItem();
                ArmorItem itemarmor1 = (ArmorItem) currentEquipped.getItem();

                if (itemarmor.getDamageReduceAmount() == itemarmor1.getDamageReduceAmount()) {
                    return stack.getDamage() > currentEquipped.getDamage() || stack.hasTag() && !currentEquipped.hasTag();
                } else {
                    return itemarmor.getDamageReduceAmount() > itemarmor1.getDamageReduceAmount();
                }
            }
        } else if (stack.getItem() instanceof BowItem) {
            if (currentEquipped.isEmpty())
                return true;
            if (currentEquipped.getItem() instanceof BowItem) {
                int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                int power2 = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, currentEquipped);
                return power > power2;
            }
        } else {
            if (currentEquipped.isEmpty())
                return true;
            return damage(stack) > damage(currentEquipped);
        }
        return false;
    }

    public static double damage(ItemStack stack) {
        double dmg = 0;
        Collection<AttributeModifier> atts = stack.getAttributeModifiers(EquipmentSlotType.MAINHAND)
                .get(Attributes.GENERIC_ATTACK_DAMAGE);
        for (AttributeModifier mod : atts) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION)
                dmg += mod.getAmount();
        }
        double value = dmg;
        for (AttributeModifier mod : atts) {
            if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                value += dmg * mod.getAmount();
        }
        for (AttributeModifier mod : atts) {
            if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                value *= 1 + mod.getAmount();
        }
        int sharp = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        if (sharp > 0)
            value += sharp * 0.5 + 0.5;
        return Attributes.GENERIC_ATTACK_DAMAGE.clampValue(value);
    }

    /**
     * Tests, if the players inventory has enough space for the itemstack without actually adding it to the inventory
     */
    public static boolean hasSpace(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        PlayerInventory inv = player.inventory;
        stack = stack.copy();
        for (ItemStack invStack : inv.mainInventory) {
            if (invStack.isEmpty()) {
                stack.setCount(stack.getCount() - stack.getMaxStackSize());
            } else if (invStack.getCount() < invStack.getMaxStackSize() && ItemStack.areItemStacksEqual(stack, invStack)) {
                int sub = invStack.getMaxStackSize() - invStack.getCount();
                stack.setCount(stack.getCount() - sub);
            }
            if (stack.getCount() <= 0) {
                break;
            }
        }
        return stack.getCount() <= 0;
    }

    /*public static boolean areItemsStackable(ItemStack stack1, ItemStack stack2) {
        return (stack1.isEmpty() && stack2.isEmpty()) || (!stack1.isEmpty() && !stack2.isEmpty() && stack1.getItem() == stack2.getItem()
                && stack1.getMetadata() == stack2.getMetadata() && ((!stack1.hasTagCompound() && !stack2.hasTagCompound()) || (stack1.hasTagCompound()
                && (stack1.getTagCompound().equals(stack2.getTagCompound()) && stack1.areCapsCompatible(stack2)))));
    }*/
}

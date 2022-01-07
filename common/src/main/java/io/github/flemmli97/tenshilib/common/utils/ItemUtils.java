package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Collection;

public class ItemUtils {

    public static boolean isItemBetter(ItemStack stack, ItemStack currentEquipped) {
        if (stack.getItem() instanceof ArmorItem) {
            if (!(currentEquipped.getItem() instanceof ArmorItem) || EnchantmentHelper.hasBindingCurse(currentEquipped))
                return true;
            else if (currentEquipped.getItem() instanceof ArmorItem itemarmor1) {
                ArmorItem itemarmor = (ArmorItem) stack.getItem();

                if (itemarmor.getDefense() == itemarmor1.getDefense()) {
                    return stack.getDamageValue() > currentEquipped.getDamageValue() || stack.hasTag() && !currentEquipped.hasTag();
                } else {
                    return itemarmor.getDefense() > itemarmor1.getDefense();
                }
            }
        } else if (stack.getItem() instanceof BowItem) {
            if (currentEquipped.isEmpty())
                return true;
            if (currentEquipped.getItem() instanceof BowItem) {
                int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                int power2 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, currentEquipped);
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
        Collection<AttributeModifier> atts = stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE);
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
        int sharp = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);
        if (sharp > 0)
            value += sharp * 0.5 + 0.5;
        return Attributes.ATTACK_DAMAGE.sanitizeValue(value);
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

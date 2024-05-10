package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class ItemUtils {

    public static boolean isItemBetter(ItemStack stack, ItemStack currentEquipped) {
        if (stack.getItem() instanceof ArmorItem) {
            if (!(currentEquipped.getItem() instanceof ArmorItem) || EnchantmentHelper.hasBindingCurse(currentEquipped))
                return true;
            else if (currentEquipped.getItem() instanceof ArmorItem armorItem1) {
                ArmorItem armorItem = (ArmorItem) stack.getItem();

                if (armorItem.getDefense() == armorItem1.getDefense()) {
                    return stack.getDamageValue() > currentEquipped.getDamageValue() || stack.getComponentsPatch().isEmpty() && !currentEquipped.getComponentsPatch().isEmpty();
                } else {
                    return armorItem.getDefense() > armorItem1.getDefense();
                }
            }
        } else if (stack.getItem() instanceof BowItem) {
            if (currentEquipped.isEmpty())
                return true;
            if (currentEquipped.getItem() instanceof BowItem) {
                int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, stack);
                int power2 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, currentEquipped);
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
        AttributeInstance m = new AttributeInstance(Attributes.ATTACK_DAMAGE, (inst) -> {
        });
        ItemAttributeModifiers stackMod = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (stackMod != null)
            stackMod.forEach(EquipmentSlot.MAINHAND, (attr, mod) -> {
                if (attr.equals(Attributes.ATTACK_DAMAGE))
                    m.addTransientModifier(mod);
            });
        double dmg = m.getValue();
        int sharp = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);
        if (sharp > 0)
            dmg += sharp * 0.5 + 0.5;
        return Attributes.ATTACK_DAMAGE.value().sanitizeValue(dmg);
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

package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
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

    public static boolean isItemBetter(LivingEntity holder, @Nullable Entity target, ItemStack stack, ItemStack currentEquipped) {
        if (target == null) {
            target = holder instanceof Mob mob && mob.getTarget() != null ? mob.getTarget() : holder;
        }
        if (stack.getItem() instanceof ArmorItem itemarmor) {
            if (!(currentEquipped.getItem() instanceof ArmorItem) || EnchantmentHelper.has(currentEquipped, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
                return true;
            else if (currentEquipped.getItem() instanceof ArmorItem itemarmor1) {
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

    @SuppressWarnings("deprecation")
    public static double attribute(ItemAttributeModifiers modifiers, Holder<Attribute> attribute, double base, EquipmentSlotGroup... groups) {
        AttributeInstance instance = new AttributeInstance(attribute, i -> {
        });
        instance.setBaseValue(base);
        if (modifiers != null) {
            for (EquipmentSlotGroup group : groups) {
                modifiers.forEach(group, (attr, mod) -> {
                    if (attr.is(attribute))
                        instance.addTransientModifier(mod);
                });
            }
        }
        return attribute.value().sanitizeValue(instance.getValue());
    }

    @SuppressWarnings("deprecation")
    public static double attribute(ItemStack stack, Holder<Attribute> attribute, double base, EquipmentSlotGroup... groups) {
        AttributeInstance instance = new AttributeInstance(attribute, i -> {
        });
        instance.setBaseValue(base);
        if (stack != null) {
            for (EquipmentSlotGroup group : groups) {
                stack.forEachModifier(group, (attr, mod) -> {
                    if (attr.is(attribute))
                        instance.addTransientModifier(mod);
                });
            }
        }
        return attribute.value().sanitizeValue(instance.getValue());
    }

    public static double damage(LivingEntity holder, @Nullable Entity target, ItemStack stack) {
        AttributeInstance attribute = holder.getAttribute(Attributes.ATTACK_DAMAGE);
        double dmg = attribute(stack, Attributes.ATTACK_DAMAGE, attribute != null ? attribute.getValue() : 1, EquipmentSlotGroup.MAINHAND);
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

package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
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
                    long enchants = currentEquipped.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                            .keySet().stream().filter(ench -> !ench.is(EnchantmentTags.CURSE)).count();
                    long enchants2 = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                            .keySet().stream().filter(ench -> !ench.is(EnchantmentTags.CURSE)).count();
                    if (enchants2 == enchants)
                        return stack.getDamageValue() > currentEquipped.getDamageValue() || stack.getComponentsPatch().isEmpty() && !currentEquipped.getComponentsPatch().isEmpty();
                    return enchants2 > enchants;
                }
                return itemarmor.getDefense() > itemarmor1.getDefense();
            }
        }
        if (currentEquipped.isEmpty())
            return true;
        double damage = damage(holder, target, stack);
        double damageCurrent = damage(holder, target, currentEquipped);
        if (damage == damageCurrent) {
            long enchants = currentEquipped.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .keySet().stream().filter(ench -> !ench.is(EnchantmentTags.CURSE)).count();
            long enchants2 = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .keySet().stream().filter(ench -> !ench.is(EnchantmentTags.CURSE)).count();
            if (enchants2 == enchants)
                return stack.getDamageValue() > currentEquipped.getDamageValue() || stack.getComponentsPatch().isEmpty() && !currentEquipped.getComponentsPatch().isEmpty();
            return enchants2 > enchants;
        }
        return damage > damageCurrent;
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
        return damage(holder.level(), holder, target, holder.damageSources().mobAttack(holder), stack);
    }

    public static double damage(Level level, @Nullable Entity holder, @Nullable Entity target, DamageSource defaultSource, ItemStack stack) {
        AttributeInstance attribute = holder instanceof LivingEntity living ? living.getAttribute(Attributes.ATTACK_DAMAGE) : null;
        double dmg = attribute(stack, Attributes.ATTACK_DAMAGE, attribute != null ? attribute.getValue() : 1, EquipmentSlotGroup.MAINHAND);
        DamageSource damageSource = defaultSource;
        if (stack.getItem() instanceof BowItem)
            damageSource = level.damageSources().arrow(EntityType.ARROW.create(level), holder);
        Entity enchantTarget = target == null ? holder : target;
        dmg = enchantTarget != null && level instanceof ServerLevel serverLevel ?
                EnchantmentHelper.modifyDamage(serverLevel, stack, enchantTarget, damageSource, (float) dmg) : dmg;
        return dmg;
    }
}

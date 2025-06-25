package io.github.flemmli97.tenshilib.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Implement for {@link net.minecraft.world.item.ArmorItem} to have dynamic armor textures in common code
 */
public interface DynamicArmorTextureItem {

    ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel);
}

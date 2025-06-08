package io.github.flemmli97.tenshilib.common.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface DynamicArmorTextureItem {

    String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type);

}

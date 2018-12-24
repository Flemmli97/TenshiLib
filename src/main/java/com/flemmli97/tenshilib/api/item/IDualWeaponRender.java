package com.flemmli97.tenshilib.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Implementing this will make items be RENDERED as dual weapons. Actual dual weapon handling will not be done
 */
public interface IDualWeaponRender {

	/**
	 * Change the item being rendered in the offhand
	 * @param entity
	 * @return
	 */
	public default ItemStack offHandStack(EntityLivingBase entity)
	{
		return entity.getHeldItemMainhand();
	}
}

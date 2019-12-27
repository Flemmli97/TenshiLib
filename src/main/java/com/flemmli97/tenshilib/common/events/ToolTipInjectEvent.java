package com.flemmli97.tenshilib.common.events;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/**
 * Like Forge's ItemTooltipEvent but gets called right after getting the items name instead of at the end. Unused atm
 */
public class ToolTipInjectEvent extends ItemTooltipEvent{

	public ToolTipInjectEvent(ItemStack itemStack, EntityPlayer entityPlayer, List<String> toolTip,
			ITooltipFlag flags) {
		super(itemStack, entityPlayer, toolTip, flags);
	}
}

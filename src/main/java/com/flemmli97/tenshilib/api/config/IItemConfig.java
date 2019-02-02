package com.flemmli97.tenshilib.api.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.NonNullList;

public interface IItemConfig extends IConfigValue, IJsonSerializable{

	public Item getItem();
	
	public ItemStack getStack();
	
	public NonNullList<ItemStack> getStackList();
	
	public boolean hasList();
}

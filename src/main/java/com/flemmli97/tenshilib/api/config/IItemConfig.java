package com.flemmli97.tenshilib.api.config;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IItemConfig extends IConfigValue, JsonDeserializer<IItemConfig>, JsonSerializer<IItemConfig>{

	public Item getItem();
	
	public ItemStack getStack();
	
	public NonNullList<ItemStack> getStackList();
	
	public boolean hasList();

}

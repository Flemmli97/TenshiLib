package com.flemmli97.tenshilib.api.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SimpleItemStackWrapper implements IConfigValue{
	
	private ItemStack item;
	
	public SimpleItemStackWrapper(ItemStack defaultItem)
	{
		this.item=defaultItem;
	}

	public ItemStack getItemStack()
	{
		return this.item;
	}

	@Override
	public IConfigValue readFromString(String s) {
		String[] parts = s.split(",");
		if(parts.length!=3)
			throw new IllegalArgumentException(s);
		Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0]));
		this.item=new ItemStack(item, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		return this;
	}

	@Override
	public String writeToString() {
		return this.item.getItem().getRegistryName().toString() + ","+this.item.getMetadata() + ","+this.item.getCount();
	}

	@Override
	public String usage() {
		return "Usage: registryname,meta,amount";
	}
}

package com.flemmli97.tenshilib.api.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemWrapper implements IConfigValue{
	
	private Item item;
	
	public ItemWrapper(Item defaultItem)
	{
		this.item=defaultItem;
	}

	public Item getItem()
	{
		return this.item;
	}
	
	public ItemStack getStack()
	{
		return new ItemStack(this.item);
	}

	@Override
	public IConfigValue readFromString(String s) {
		Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
		if(item==null)
			throw new NullPointerException(s);
		this.item=item;
		return this;
	}

	@Override
	public String writeToString() {
		return this.item.getRegistryName().toString();
	}

	@Override
	public String usage() {
		return "Valid values are all item registry names";
	}
}

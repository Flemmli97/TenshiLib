package com.flemmli97.tenshilib.api.config;

import com.flemmli97.tenshilib.TenshiLib;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SimpleItemStackWrapper extends ItemWrapper{
	
	protected int meta;
	protected int count;
	
	public SimpleItemStackWrapper(String s)
	{
		super(s);
	}
	
	public SimpleItemStackWrapper(Item item)
	{
		this(item, 1);
	}
	
	public SimpleItemStackWrapper(Item item, int amount)
	{
		this(item, -1, amount);
	}
	
	public SimpleItemStackWrapper(Block block)
	{
		this(Item.getItemFromBlock(block), -1, 1);
	}
	
	public SimpleItemStackWrapper(ItemStack stack)
	{
		this(stack.getItem(), stack.getMetadata(), stack.getCount());
	}
	
	public SimpleItemStackWrapper(ItemStack stack, boolean ignoreMeta)
	{
		this(stack.getItem(), ignoreMeta?-1:stack.getMetadata(), stack.getCount());
	}
	
	public SimpleItemStackWrapper(Item defaultItem, int meta, int amount)
	{
		super(defaultItem);
		this.meta=meta;
		this.count=amount;	
	}
	
	public SimpleItemStackWrapper setIgnoreMeta()
	{
		this.meta=-1;
		return this;
	}
	
	public SimpleItemStackWrapper setIgnoreAmount()
	{
		this.count=1;
		return this;
	}

	@Override
	public ItemStack getStack()
	{
		return new ItemStack(this.item, this.meta==-1?0:this.meta, this.count);
	}

	@Override
	public IConfigValue readFromString(String s) {
		String[] parts = s.split(",");
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0]));
		if(item==null || item == Items.AIR)
			TenshiLib.logger.error("Faulty item registry name {}; Full String {}", parts[0], s);
		else
		{
			this.item=item;
			this.meta=parts.length<2?-1:Integer.parseInt(parts[1]);
			this.count=parts.length<3?1:Integer.parseInt(parts[2]);
		}
		return this;
	}

	@Override
	public String writeToString() {
		return this.item.getRegistryName().toString() + (this.meta!=-1?","+this.meta:"") + (this.count!=1?","+this.count:"");
	}

	@Override
	public String usage() {
		return "Usage: registryname<,meta><,amount>";
	}
	
	@Override
	public void fromJson(JsonElement json) {
		if(json instanceof JsonObject)
		{
			JsonObject obj = (JsonObject) json;
			if(obj.get("item") instanceof JsonPrimitive)
				this.item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString()));
			if(obj.get("meta") instanceof JsonPrimitive && ((JsonPrimitive)obj.get("meta")).isNumber())
				this.meta=obj.get("meta").getAsInt();
			if(obj.get("count") instanceof JsonPrimitive && ((JsonPrimitive)obj.get("count")).isNumber())
				this.count=obj.get("count").getAsInt();
		}
	}

	@Override
	public JsonElement getSerializableElement() {
		JsonObject obj = new JsonObject();
		obj.add("item", new JsonPrimitive(this.item.getRegistryName().toString()));
		obj.add("meta", new JsonPrimitive(this.meta));
		obj.add("count", new JsonPrimitive(this.count));
		return obj;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) 
        {
            return true;
        }
        if (obj instanceof SimpleItemStackWrapper) 
        {
        	SimpleItemStackWrapper prop = (SimpleItemStackWrapper)obj;
            return prop.toString().equals(this.toString());
        }
        return false;
    }
}

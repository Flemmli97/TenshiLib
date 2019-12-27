package com.flemmli97.tenshilib.api.config;

import java.lang.reflect.Type;

import com.flemmli97.tenshilib.TenshiLib;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
		return this.item==null ? ItemStack.EMPTY : new ItemStack(this.item, this.count, this.meta==-1?0:this.meta);
	}

	@Override
	public SimpleItemStackWrapper readFromString(String s) {
		if(s.isEmpty())
		{
			this.item=null;
			return this;
		}
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
		return "Usage: registryname<,meta><,amount>. Leave empty for no item";
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
	
	public static class Serializer implements JsonDeserializer<SimpleItemStackWrapper>, JsonSerializer<SimpleItemStackWrapper>
	{

		@Override
		public JsonElement serialize(SimpleItemStackWrapper src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.add("item", new JsonPrimitive(src.item.getRegistryName().toString()));
			if(src.meta!=-1)
				obj.add("meta", new JsonPrimitive(src.meta));
			if(src.count!=1)
				obj.add("count", new JsonPrimitive(src.count));
			return obj;
		}

		@Override
		public SimpleItemStackWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = (JsonObject) json.getAsJsonObject();
			int meta = -1;
			int count = 1;
			if(obj.get("meta") instanceof JsonPrimitive && obj.get("meta").getAsJsonPrimitive().isNumber())
				meta=obj.get("meta").getAsInt();
			if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
				count=obj.get("count").getAsInt();
			return new SimpleItemStackWrapper(ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString())), meta, count);
		}	
	}
}

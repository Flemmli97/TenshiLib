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

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemWrapper implements IItemConfig{
	
	protected Item item = Items.AIR;
	
	public ItemWrapper(Item defaultItem)
	{
		this.item=defaultItem;
	}
	
	public ItemWrapper(String s)
	{
		this.readFromString(s);
	}

	@Override
	public Item getItem()
	{
		return this.item;
	}
	
	@Override
	public ItemStack getStack()
	{
		return new ItemStack(this.item);
	}
	

	@Override
	public NonNullList<ItemStack> getStackList() {
		return null;
	}

	@Override
	public boolean hasList() {
		return false;
	}

	@Override
	public IConfigValue readFromString(String s) {
		Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
		if(item==null)
			TenshiLib.logger.error("Faulty item registry name {}", s);
		else
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
	

	@Override
	public IItemConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new ItemWrapper(ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonUtils.getJsonObject(json, "item").get("item").getAsString())));
	}

	@Override
	public JsonElement serialize(IItemConfig src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		obj.add("item", new JsonPrimitive(this.item.getRegistryName().toString()));
		return obj;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) 
        {
            return true;
        }
        if (obj instanceof ItemWrapper) 
        {
        	ItemWrapper prop = (ItemWrapper)obj;
            return prop.writeToString().equals(this.writeToString());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.writeToString().hashCode();
    }
    
    @Override
    public String toString() {
        return this.writeToString();
    }
    	
	public static class Serializer implements JsonDeserializer<ItemWrapper>, JsonSerializer<ItemWrapper>
	{

		@Override
		public JsonElement serialize(ItemWrapper src, Type typeOfSrc, JsonSerializationContext context) {
			return src.serialize(src, typeOfSrc, context);
		}

		@Override
		public ItemWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return new ItemWrapper(ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonUtils.getJsonObject(json, "item").get("item").getAsString())));
		}	
	}
}

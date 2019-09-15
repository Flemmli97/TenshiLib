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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemWrapper implements IItemConfig<ItemWrapper>{
	
	protected Item item;
	
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
		return this.item==null ? ItemStack.EMPTY : new ItemStack(this.item);
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
	public ItemWrapper readFromString(String s) {
		if(s.isEmpty())
		{
			this.item=null;
			return this;
		}
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
		return "Valid values are all item registry names. Empty for nothing";
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
			JsonObject obj = new JsonObject();
			obj.add("item", new JsonPrimitive(src.item.getRegistryName().toString()));
			return obj;		
		}

		@Override
		public ItemWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return new ItemWrapper(ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonUtils.getJsonObject(json, "item").get("item").getAsString())));
		}	
	}
}

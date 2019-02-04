package com.flemmli97.tenshilib.api.config;

import java.lang.reflect.Type;

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
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class ItemOredictWrapper extends SimpleItemStackWrapper{

	private String oreDict;
	private ItemStack firstOreDict = ItemStack.EMPTY;
	private NonNullList<ItemStack> list;
	
	public ItemOredictWrapper(String oredictName) {
		this(oredictName, 1);
	}
	public ItemOredictWrapper(String oredictName, int amount) {
		super(Items.AIR, amount);
		this.oreDict=oredictName;
		this.reloadItem();
	}
	
	private void reloadItem()
	{
		this.list = OreDictionary.getOres(this.oreDict);
		if(!list.isEmpty())
			this.firstOreDict=list.get(0);
		this.firstOreDict.setCount(this.count);
	}

	@Override
	public ItemStack getStack()
	{
		return firstOreDict;
	}
	
	@Override
	public Item getItem()
	{
		return this.firstOreDict.getItem();
	}

	@Override
	public NonNullList<ItemStack> getStackList() {
		return this.list;
	}

	@Override
	public boolean hasList() {
		return true;
	}

	@Override
	public IConfigValue readFromString(String s) {
		String[] parts = s.split(",");
		this.oreDict=parts[0];
		this.reloadItem();
		return this;
	}

	@Override
	public String writeToString() {
		return this.oreDict + (this.count!=1?","+this.count:"");
	}

	@Override
	public String usage() {
		return "Usage: oreDict<,amount>";
	}
	
	
	@Override
	public IItemConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	{
		JsonObject obj = json.getAsJsonObject();
		int count = 1;
		if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
			count=obj.get("count").getAsInt();
		return new ItemOredictWrapper(obj.get("oredict").getAsString(), count);
	}

	@Override
	public JsonElement serialize(IItemConfig src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		obj.add("oredict", new JsonPrimitive(this.oreDict));
		if(this.count!=1)
			obj.add("count", new JsonPrimitive(this.count));
		return obj;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) 
        {
            return true;
        }
        if (obj instanceof ItemOredictWrapper) 
        {
        	ItemOredictWrapper prop = (ItemOredictWrapper)obj;
            return prop.writeToString().equals(this.toString());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.writeToString().hashCode();
    }
	
	public static class Serializer implements JsonDeserializer<ItemOredictWrapper>, JsonSerializer<ItemOredictWrapper>
	{

		@Override
		public JsonElement serialize(ItemOredictWrapper src, Type typeOfSrc, JsonSerializationContext context) {
			return src.serialize(src, typeOfSrc, context);
		}

		@Override
		public ItemOredictWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			int count = 1;
			if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
				count=obj.get("count").getAsInt();
			return new ItemOredictWrapper(obj.get("oredict").getAsString(), count);
		}	
	}
}

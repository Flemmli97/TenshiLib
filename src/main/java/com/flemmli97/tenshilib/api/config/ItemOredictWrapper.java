package com.flemmli97.tenshilib.api.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
		super(Items.AIR, -1);
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
		this.count=parts.length<1?1:Integer.parseInt(parts[1]);
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
	public void fromJson(JsonElement json) {
		if(json instanceof JsonObject)
		{
			JsonObject obj = (JsonObject) json;
			if(obj.get("oredict") instanceof JsonPrimitive)
				this.oreDict=obj.get("oredict").getAsString();
			if(obj.get("count") instanceof JsonPrimitive && ((JsonPrimitive)obj.get("count")).isNumber())
				this.count=obj.get("count").getAsInt();
			this.reloadItem();

		}
	}

	@Override
	public JsonElement getSerializableElement() {
		JsonObject obj = new JsonObject();
		obj.add("oredict", new JsonPrimitive(this.oreDict));
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
}

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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ExtendedItemStackWrapper extends SimpleItemStackWrapper{

	private NBTTagCompound nbtTagCompound;

	public ExtendedItemStackWrapper(NBTTagCompound compound)
	{
		this(new ItemStack(compound));
	}
	
	public ExtendedItemStackWrapper(Item item)
	{
		super(item, 1);
	}
	
	public ExtendedItemStackWrapper(Item item, int amount)
	{
		super(item, -1, amount);
	}
	
	public ExtendedItemStackWrapper(Block block)
	{
		super(Item.getItemFromBlock(block), -1, 1);
	}
	
	public ExtendedItemStackWrapper(ItemStack stack)
	{
		super(stack.getItem(), stack.getMetadata(), stack.getCount());
		this.nbtTagCompound = stack.hasTagCompound()?stack.getTagCompound():null;
	}
	
	public ExtendedItemStackWrapper(ItemStack stack, boolean ignoreMeta)
	{
		super(stack.getItem(), ignoreMeta?-1:stack.getMetadata(), stack.getCount());
	}
	
	public ExtendedItemStackWrapper ignoreNBT()
	{
		this.nbtTagCompound=null;
		return this;
	}
	
	@Override
	public IConfigValue readFromString(String s) {
		return this;
	}

	@Override
	public String writeToString() {
		return super.writeToString()+(this.nbtTagCompound!=null?",nbt:"+this.nbtTagCompound.toString():"");
	}

	@Override
	public String usage() {
		return "";
	}

	@Override
	public IItemConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	{
		JsonObject obj = (JsonObject) json.getAsJsonObject();
		int meta = -1;
		int count = 1;
		if(obj.get("meta") instanceof JsonPrimitive && obj.get("meta").getAsJsonPrimitive().isNumber())
			meta=obj.get("meta").getAsInt();
		if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
			count=obj.get("count").getAsInt();
		JsonObject nbt = obj.has("nbt")?obj.get("nbt").getAsJsonObject():null;
		NBTTagCompound compound = null;
		if(nbt!=null)
		{
			try {
				compound=JsonToNBT.getTagFromJson(nbt.toString());
			} catch (NBTException e) {
				e.printStackTrace();
			}
		}
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString())), meta, count);
		stack.setTagCompound(compound);
		return new ExtendedItemStackWrapper(stack);
	}

	@Override
	public JsonElement serialize(IItemConfig src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject obj = (JsonObject) super.serialize(src, typeOfSrc, context);
		if(this.nbtTagCompound!=null)
			obj.add("nbt", new JsonPrimitive(this.nbtTagCompound.toString()));
		return obj;
	}

	@Override
	public ItemStack getStack() {
		ItemStack stack = new ItemStack(this.item, this.count, this.meta==-1?0:this.meta);
		stack.setTagCompound(this.nbtTagCompound);
		return stack;
	}
	
	public static class Serializer implements JsonDeserializer<ExtendedItemStackWrapper>, JsonSerializer<ExtendedItemStackWrapper>
	{

		@Override
		public JsonElement serialize(ExtendedItemStackWrapper src, Type typeOfSrc, JsonSerializationContext context) {
			return src.serialize(src, typeOfSrc, context);
		}

		@Override
		public ExtendedItemStackWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = (JsonObject) json.getAsJsonObject();
			int meta = -1;
			int count = 1;
			if(obj.get("meta") instanceof JsonPrimitive && obj.get("meta").getAsJsonPrimitive().isNumber())
				meta=obj.get("meta").getAsInt();
			if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
				count=obj.get("count").getAsInt();
			JsonObject nbt = obj.has("nbt")?obj.get("nbt").getAsJsonObject():null;
			NBTTagCompound compound = null;
			if(nbt!=null)
			{
				try {
					compound=JsonToNBT.getTagFromJson(nbt.toString());
				} catch (NBTException e) {
					e.printStackTrace();
				}
			}
			ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString())), count, meta==-1?0:meta);
			stack.setTagCompound(compound);
			ExtendedItemStackWrapper w = new ExtendedItemStackWrapper(stack);
			if(meta==-1)
				w.setIgnoreMeta();
			return w;
		}	
	}
}

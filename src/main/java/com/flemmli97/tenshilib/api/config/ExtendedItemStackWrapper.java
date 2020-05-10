package com.flemmli97.tenshilib.api.config;

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

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Itemstack supporting nbt. Not exactly made as a single line string config value. More as a json one.
 */
public class ExtendedItemStackWrapper extends SimpleItemStackWrapper {

    private NBTTagCompound nbtTagCompound;

    public ExtendedItemStackWrapper(NBTTagCompound compound) {
        this(new ItemStack(compound));
    }

    public ExtendedItemStackWrapper(Item item) {
        super(item, 1);
    }

    public ExtendedItemStackWrapper(Item item, int amount) {
        super(item, -1, amount);
    }

    public ExtendedItemStackWrapper(Block block) {
        super(Item.getItemFromBlock(block), -1, 1);
    }

    public ExtendedItemStackWrapper(ItemStack stack) {
        super(stack.getItem(), stack.getMetadata(), stack.getCount());
        this.nbtTagCompound = stack.hasTagCompound() ? stack.getTagCompound().copy() : null;
    }

    public ExtendedItemStackWrapper(ItemStack stack, boolean ignoreMeta) {
        super(stack.getItem(), ignoreMeta ? -1 : stack.getMetadata(), stack.getCount());
    }

    public ExtendedItemStackWrapper setNBT(NBTTagCompound nbt) {
        this.nbtTagCompound = nbt;
        return this;
    }

    public ExtendedItemStackWrapper ignoreNBT() {
        this.nbtTagCompound = null;
        return this;
    }

    @Override
    public ExtendedItemStackWrapper readFromString(String s) {
        return this;
    }

    @Override
    public String writeToString() {
        return super.writeToString() + (this.nbtTagCompound != null ? ",nbt:" + this.nbtTagCompound.toString() : "");
    }

    @Override
    public String usage() {
        return "";
    }

    @Nullable
    public NBTTagCompound getTag() {
        return this.nbtTagCompound == null ? null : this.nbtTagCompound.copy();
    }

    @Override
    public ItemStack getStack() {
        if(this.item == null)
            return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(this.item, this.count, this.meta == -1 ? 0 : this.meta);
        if(this.nbtTagCompound != null)
            stack.setTagCompound(this.nbtTagCompound.copy());
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof ExtendedItemStackWrapper){
            ExtendedItemStackWrapper prop = (ExtendedItemStackWrapper) obj;
            return prop.toString().equals(this.toString());
        }
        return false;
    }

    public static class Serializer implements JsonDeserializer<ExtendedItemStackWrapper>, JsonSerializer<ExtendedItemStackWrapper> {

        @Override
        public JsonElement serialize(ExtendedItemStackWrapper src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("item", new JsonPrimitive(src.item.getRegistryName().toString()));
            if(src.meta != -1)
                obj.add("meta", new JsonPrimitive(src.meta));
            if(src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            if(src.nbtTagCompound != null)
                obj.add("nbt", new JsonPrimitive(src.nbtTagCompound.toString()));
            return obj;
        }

        @Override
        public ExtendedItemStackWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int meta = -1;
            int count = 1;
            if(obj.get("meta") instanceof JsonPrimitive && obj.get("meta").getAsJsonPrimitive().isNumber())
                meta = obj.get("meta").getAsInt();
            if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
                count = obj.get("count").getAsInt();
            JsonObject nbt = obj.has("nbt") ? obj.get("nbt").getAsJsonObject() : null;
            NBTTagCompound compound = null;
            if(nbt != null){
                try{
                    compound = JsonToNBT.getTagFromJson(nbt.toString());
                }catch(NBTException e){
                    e.printStackTrace();
                }
            }
            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString())), count,
                    meta == -1 ? 0 : meta);
            stack.setTagCompound(compound);
            ExtendedItemStackWrapper w = new ExtendedItemStackWrapper(stack);
            if(meta == -1)
                w.setIgnoreMeta();
            return w;
        }
    }
}

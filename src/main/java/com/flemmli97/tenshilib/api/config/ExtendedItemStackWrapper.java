package com.flemmli97.tenshilib.api.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Itemstack supporting nbt. Not exactly made as a single line string config value. More as a json one.
 */
public class ExtendedItemStackWrapper extends SimpleItemStackWrapper {

    private CompoundNBT nbtTagCompound;

    public ExtendedItemStackWrapper(CompoundNBT compound) {
        this(ItemStack.read(compound));
    }

    public ExtendedItemStackWrapper(Item item) {
        super(item, 1);
    }

    public ExtendedItemStackWrapper(Item item, int amount) {
        super(item, amount);
    }

    public ExtendedItemStackWrapper(Block block) {
        super(block.asItem(), 1);
    }

    public ExtendedItemStackWrapper(ItemStack stack) {
        super(stack.getItem(), stack.getCount());
        this.nbtTagCompound = stack.hasTag() ? stack.getTag().copy() : null;
    }

    public ExtendedItemStackWrapper setNBT(CompoundNBT nbt) {
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
    public CompoundNBT getTag() {
        return this.nbtTagCompound == null ? null : this.nbtTagCompound.copy();
    }

    @Override
    public ItemStack getStack() {
        if(this.item == null)
            return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(this.item, this.count);
        if(this.nbtTagCompound != null)
            stack.setTag(this.nbtTagCompound.copy());
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
            if(src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            if(src.nbtTagCompound != null)
                obj.add("nbt", new JsonPrimitive(src.nbtTagCompound.toString()));
            return obj;
        }

        @Override
        public ExtendedItemStackWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int count = 1;
            if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
                count = obj.get("count").getAsInt();
            JsonObject nbt = obj.has("nbt") ? obj.get("nbt").getAsJsonObject() : null;
            CompoundNBT compound = null;
            if(nbt != null){
                try{
                    compound = JsonToNBT.getTagFromJson(nbt.toString());
                }catch(CommandSyntaxException e){
                    e.printStackTrace();
                }
            }
            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(obj.get("item").getAsString())), count);
            stack.setTag(compound);
            ExtendedItemStackWrapper w = new ExtendedItemStackWrapper(stack);
            return w;
        }
    }
}

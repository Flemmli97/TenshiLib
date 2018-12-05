package com.flemmli97.tenshilib.common;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InitUtils {

	public static void registerAllBlocks(Class<?> clss, RegistryEvent.Register<Block> event)
	{
		for(Field field : clss.getDeclaredFields())
		{
			if(field.getType().isAssignableFrom(Block.class))
			{
				try {
					event.getRegistry().register((Block) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void registerAllItems(Class<?> clss, RegistryEvent.Register<Item> event)
	{
		for(Field field : clss.getDeclaredFields())
		{
			if(field.getType().isAssignableFrom(Item.class))
			{
				try {
					event.getRegistry().register((Item) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static final ItemBlock itemFromBlock(Block block)
	{
		ItemBlock item = new ItemBlock(block);
		item.setRegistryName(block.getRegistryName());
		return item;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerSpecificModel(Block block, ResourceLocation regName) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(regName, "inventory"));
    }
	
	@SideOnly(Side.CLIENT)
	public static void registerDefaultModel(Block block) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }
	
	@SideOnly(Side.CLIENT)
	public static void registerDefaultModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
	
}

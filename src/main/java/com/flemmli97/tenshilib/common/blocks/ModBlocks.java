package com.flemmli97.tenshilib.common.blocks;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.InitUtils;
import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = TenshiLib.MODID)
public class ModBlocks {
	
	public static final Block ignore = new BlockIgnore("ignore");
	public static final Block structure = new BlockStructurePiece();

	@SubscribeEvent
	public static final void registerBlocks(RegistryEvent.Register<Block> event) { 
	    event.getRegistry().register(ignore);
	    event.getRegistry().register(structure);
	    GameRegistry.registerTileEntity(TileStructurePiece.class, new ResourceLocation(TenshiLib.MODID, "tile_structure_piece"));

	}
	
	@SubscribeEvent
    public static final void registerItemBlocks(RegistryEvent.Register<Item> event) {
	    event.getRegistry().register(InitUtils.itemFromBlock(ignore));
	    event.getRegistry().register(InitUtils.itemFromBlock(structure));

	}
	
	@SubscribeEvent
	@SideOnly(value = Side.CLIENT)
	public static final void initModel(ModelRegistryEvent event)
	{
		InitUtils.registerSpecificModel(ignore, Blocks.END_ROD.getRegistryName());
		InitUtils.registerSpecificModel(structure, Blocks.END_ROD.getRegistryName());
	}
}

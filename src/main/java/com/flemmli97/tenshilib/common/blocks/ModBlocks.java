package com.flemmli97.tenshilib.common.blocks;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.ItemBlockInitUtils;
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
    public static final Block breakableBarrier = new BlockBreakableBarrier();
    //public static final Block camo = new BlockCamo(new ResourceLocation(TenshiLib.MODID, "camo"), Material.ROCK);

    @SubscribeEvent
    public static final void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ignore);
        event.getRegistry().register(structure);
        event.getRegistry().register(breakableBarrier);
        //event.getRegistry().register(camo);
        GameRegistry.registerTileEntity(TileStructurePiece.class, new ResourceLocation(TenshiLib.MODID, "tile_structure_piece"));
        //GameRegistry.registerTileEntity(TileCamo.class, new ResourceLocation(TenshiLib.MODID, "tile_camo"));
    }

    @SubscribeEvent
    public static final void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ItemBlockInitUtils.itemFromBlock(ignore));
        event.getRegistry().register(ItemBlockInitUtils.itemFromBlock(structure));
        event.getRegistry().register(ItemBlockInitUtils.itemFromBlock(breakableBarrier));
        //event.getRegistry().register(ItemBlockInitUtils.itemFromBlock(camo));

    }

    @SubscribeEvent
    @SideOnly(value = Side.CLIENT)
    public static final void initModel(ModelRegistryEvent event) {
        ItemBlockInitUtils.registerSpecificModel(ignore, Blocks.END_ROD.getRegistryName());
        ItemBlockInitUtils.registerSpecificModel(structure, Blocks.STRUCTURE_BLOCK.getRegistryName());
        ItemBlockInitUtils.registerSpecificModel(breakableBarrier, Blocks.BARRIER.getRegistryName());
        //ItemBlockInitUtils.registerSpecificModel(camo, Blocks.STONE.getRegistryName());
    }
}

package com.flemmli97.tenshilib.common.world;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.flemmli97.tenshilib.TenshiLib;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;

public class StructureLoader {
	
	private static final Map<ResourceLocation, Schematic> schemMap = Maps.newHashMap();
	
	public static Schematic getSchematic(ResourceLocation res)
	{
		if(schemMap.get(res)==null)
			schemMap.put(res, getSchematic(res.getResourceDomain(), res.getResourcePath()));
		return schemMap.get(res);
	}
	public static Schematic getSchematic(String modid, String fileName)
	{
		try {
			/*InputStream input = Loader.class.getResourceAsStream("/assets/"+modid+"/structures/" + fileName + ".schematic");
			if(input!=null)
				return loadSchematic(input, world);*/
			InputStream input = Loader.class.getResourceAsStream("/assets/"+modid+"/structures/" + fileName + ".nbt");
			if(input!=null)
				return loadStructureBlockFile(input);
			throw new FileNotFoundException();
		}
		catch(FileNotFoundException e)
		{
			TenshiLib.logger.error("Error reading file {}", fileName);
		}
		return null;
	}
	
	//look into it more, for now not supported
	/*private static Schematic loadSchematic(InputStream input, World world)
	{
		try {
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(input);
			short width = nbt.getShort("Width");
			short height = nbt.getShort("Height");
			short length = nbt.getShort("Length");
			byte[] blocks = nbt.getByteArray("Blocks");
			byte[] data = nbt.getByteArray("Data");
			NBTTagList tileEntities = nbt.getTagList("TileEntities", 10);	
			ArrayList<TileEntity> tiles = new ArrayList<TileEntity>();
			Map<Position, IBlockState> posBlockMapping = Maps.newHashMap();
			NBTTagCompound idNameMap = nbt.getCompoundTag("SchematicaMapping");
			System.out.println(idNameMap);
			for (int x = 0; x < width; x++) {
	            for (int y = 0; y < height; y++) {
	                for (int z = 0; z < length; z++) {
	                    final int index = x + (y * length + z) * width;
	                    int blockID = blocks[index] & 0xFF;
	                    final int meta = data[index] & 0xFF;
	                    final Block block = Block.REGISTRY.getObjectById(blockID);
	                    Position pos = new Position(x, y, z);
	                    try {
	                        @SuppressWarnings("deprecation")
							final IBlockState blockState = block.getStateFromMeta(meta);
	                        posBlockMapping.put(pos, blockState);
	                    } catch (final Exception e) {
	                        RuneCraftory.logger.error("Error with block state at {"+pos+"} with block {"+block+"} and metadata {"+meta+"}");
	                    }
	                }
	            }
			}	                 
	                   
			for(int i = 0; i < tileEntities.tagCount(); i++)
			{
				tiles.add(TileEntity.create(world, tileEntities.getCompoundTagAt(i)));
			}
			Schematic schematic = new Schematic(width, height, length);		
			return schematic;
		} 
		catch (IOException e) {
			RuneCraftory.logger.error("Error reading schematic file", e);
			return null;
		}
	}*/

	private static Schematic loadStructureBlockFile(InputStream input)
	{
		try {
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(input);
			return loadFromNBT(nbt);
		} 
		catch (IOException e) {
			TenshiLib.logger.error("Error reading nbt file", e);
			return null;
		}
	}
	
	public static Schematic loadFromNBT(NBTTagCompound nbt)
	{
		NBTTagList size = nbt.getTagList("size", Constants.NBT.TAG_INT);
		NBTTagList palette = nbt.getTagList("palette", Constants.NBT.TAG_COMPOUND);
		NBTTagList blocks = nbt.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
		Schematic schematic = new Schematic(size.getIntAt(0), size.getIntAt(1), size.getIntAt(2));		

		for(int i = 0;i < blocks.tagCount(); i++)
		{
			NBTTagList a = blocks.getCompoundTagAt(i).getTagList("pos", Constants.NBT.TAG_INT);
			Position pos = new Position(a.getIntAt(0), a.getIntAt(1), a.getIntAt(2));
			int stateId = blocks.getCompoundTagAt(i).getInteger("state");				
			IBlockState state = NBTUtil.readBlockState(palette.getCompoundTagAt(stateId));
			schematic.addBlockStateToPos(pos, state);
			if(blocks.getCompoundTagAt(i).hasKey("nbt"))
			{
				NBTTagCompound tileData= blocks.getCompoundTagAt(i).getCompoundTag("nbt");
				schematic.addTileToPos(pos, tileData);
			}
		}
		return schematic;
	}
}

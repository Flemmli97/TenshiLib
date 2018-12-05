package com.flemmli97.tenshilib.common.world;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.flemmli97.tenshilib.TenshiLib;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class StructureGenerator implements IWorldGenerator{

	private static Map<ResourceLocation, Structure> gens = Maps.newHashMap();
	
	public static void registerStructure(Structure structure)
	{
		gens.put(structure.structureName(), structure);
		TenshiLib.logger.info("Registered structure with id {}", structure.structureName());
	}
	
	public static Set<ResourceLocation> allRegisteredStructures()
	{
		return gens.keySet();
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider) {
		if(world.getWorldInfo().isMapFeaturesEnabled())
		{
			for(Structure s : gens.values())
			{
				s.startStructure(world, chunkX, chunkZ, random);
			}
			StructureMap.get(world).generate(world, random, chunkX, chunkZ);
		}
	}
}

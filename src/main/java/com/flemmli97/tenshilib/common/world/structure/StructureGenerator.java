package com.flemmli97.tenshilib.common.world.structure;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.config.ConfigHandler;
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
			//new Thread() {
			//	@Override
			//	public void run() {
					for(Structure s : gens.values())
					{
						for(int x = chunkX-ConfigHandler.generatorRadius; x <=chunkX+ConfigHandler.generatorRadius; x++)
							for(int z = chunkZ-ConfigHandler.generatorRadius; z <=chunkZ+ConfigHandler.generatorRadius; z++)
							{
								world.setRandomSeed(x, z, ConfigHandler.seed);
								s.startStructure(world, x, z, random);
							}
					}
			//	}}.start();
			StructureMap.get(world).generate(world, chunkX, chunkZ);
		}
	}
}

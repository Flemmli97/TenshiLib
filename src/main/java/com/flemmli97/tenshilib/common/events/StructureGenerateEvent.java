package com.flemmli97.tenshilib.common.events;

import com.flemmli97.tenshilib.common.world.structure.Structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StructureGenerateEvent extends Event{
	
	private final Structure structure;
	private final BlockPos pos;
	private final World world;
	public StructureGenerateEvent(Structure structure, BlockPos pos, World world)
	{
		this.structure=structure;
		this.pos=pos;
		this.world=world;
	}

	public Structure getStructure()
	{
		return this.structure;
	}
	
	public BlockPos getPos()
	{
		return this.pos;
	}
	
	public World getWorld()
	{
		return this.world;
	}
}

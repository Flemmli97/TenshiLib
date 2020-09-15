package com.flemmli97.tenshilib.api.block;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Used by Structures. Allows modification to tile entities e.g. Spawning a mob during generation.
 */
public interface ITileEntityInitialPlaced {

    void onPlaced(World world, BlockPos pos, Rotation rot, Mirror mirror);
}

package com.flemmli97.tenshilib.common.world;

import com.flemmli97.tenshilib.TenshiLib;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class Particles {

    public static void spawnParticle(ResourceLocation res, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed,
            double zSpeed, Object... parameters) {
        TenshiLib.proxy.spawnParticle(res, world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }
}

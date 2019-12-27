package com.flemmli97.tenshilib.client.particles;

import java.util.Map;

import com.flemmli97.tenshilib.TenshiLib;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ParticleHandler {

    //public static List<ResourceLocation> particleNames = Lists.newArrayList();

    public static void spawnParticle(ResourceLocation res, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed,
            double zSpeed, Object... parameters) {
        TenshiLib.proxy.spawnParticle(res, world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @SideOnly(Side.CLIENT)
    public static class ParticleRegistries {

        private static final Map<ResourceLocation, IParticleCreator> particles = Maps.newHashMap();

        public static void registerParticle(ResourceLocation res, IParticleCreator factory) {
            particles.put(res, factory);
        }

        public static void spawnParticle(ResourceLocation res, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed,
                double zSpeed, Object... parameters) {
            IParticleCreator iparticlefactory = particles.get(res);
            if(iparticlefactory != null){
                spawnParticle(iparticlefactory.createParticle(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters));
            }
        }

        public static void spawnParticle(Particle particle) {
            if(particle != null){
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }
}

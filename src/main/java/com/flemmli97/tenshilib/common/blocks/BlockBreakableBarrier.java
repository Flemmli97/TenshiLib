package com.flemmli97.tenshilib.common.blocks;

import java.util.Random;

import com.flemmli97.tenshilib.TenshiLib;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBreakableBarrier extends Block {

    public BlockBreakableBarrier() {
        super(Material.CIRCUITS);
        this.setHardness(0.1f);
        this.setResistance(100);
        this.setRegistryName(new ResourceLocation(TenshiLib.MODID, "breakable_barrier"));
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.disableStats();
        this.translucent = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
        if(Minecraft.getMinecraft().player.isCreative())
            world.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D, new int[0]);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }
}

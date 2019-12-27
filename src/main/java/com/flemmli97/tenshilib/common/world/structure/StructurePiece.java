package com.flemmli97.tenshilib.common.world.structure;

import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;

public class StructurePiece {

    private ResourceLocation structureToGen;
    private BlockPos pos;
    private Mirror mirror = Mirror.NONE;
    private Rotation rot = Rotation.NONE;
    private GenerationType genType;

    private Set<ChunkPos> structureChunks = Sets.newHashSet();

    public StructurePiece(ResourceLocation structure, Mirror mirror, Rotation rot, GenerationType genType, BlockPos pos,
            @Nullable StructureBase parent, Random rand) {
        this.structureToGen = structure;
        this.pos = pos;
        this.mirror = mirror;
        this.rot = rot;
        this.genType = genType;
        Schematic schematic = StructureLoader.getSchematic(structure);
        StructureBoundingBox box = StructureBase.getBox(schematic, rot, pos);
        this.structureChunks = StructureBase.calculateChunks(box);
        if(parent != null && !parent.intersects(box)){
            //Add structure piece to main structure
            if(parent.addStructurePiece(this)){
                //Keep adding possible pieces to main structure
                parent.addParts(schematic, pos, rand);
                //Add the bounding box of this piece to main structure
                parent.expand(box);
            }

        }
    }

    public StructurePiece(NBTTagCompound compound) {
        this.readFromNBT(compound);
    }

    /**
     * Generate without restriction. Used by TileStructurePiece
     */
    public void generate(World world) {
        StructureLoader.getSchematic(this.structureToGen).generate(world, this.pos, this.rot, this.mirror, this.genType, null, true);
    }

    /**
     * Generate this piece with the given chunk as restriction.
     */
    public boolean generate(World world, int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if(this.structureChunks.contains(pos)){
            StructureLoader.getSchematic(this.structureToGen).generate(world, this.pos, this.rot, this.mirror, this.genType,
                    StructureBase.getChunk(chunkX, chunkZ), true);
            this.structureChunks.remove(pos);
        }
        return this.structureChunks.isEmpty();
    }

    public void readFromNBT(NBTTagCompound compound) {
        int[] arrPos = compound.getIntArray("Position");
        this.pos = new BlockPos(arrPos[0], arrPos[1], arrPos[2]);
        this.mirror = Mirror.valueOf(compound.getString("Mirror"));
        this.rot = Rotation.valueOf(compound.getString("Rotation"));
        this.genType = GenerationType.valueOf(compound.getString("GenerationType"));
        this.structureToGen = new ResourceLocation(compound.getString("StructureToGenerate"));
        compound.getTagList("StructureChunks", Constants.NBT.TAG_INT_ARRAY).forEach(tag -> {
            int[] arr = ((NBTTagIntArray) tag).getIntArray();
            this.structureChunks.add(new ChunkPos(arr[0], arr[1]));
        });
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setIntArray("Position", new int[] {this.pos.getX(), this.pos.getY(), this.pos.getZ()});
        compound.setString("Mirror", this.mirror.toString());
        compound.setString("Rotation", this.rot.toString());
        compound.setString("GenerationType", this.genType.toString());
        compound.setString("StructureToGenerate", this.structureToGen.toString());
        NBTTagList list = new NBTTagList();
        this.structureChunks.forEach(pos -> list.appendTag(new NBTTagIntArray(new int[] {pos.x, pos.z})));
        compound.setTag("StructureChunks", list);
        return compound;
    }

    @Override
    public String toString() {
        return "Schematic:" + this.structureToGen + ",Mirror:" + this.mirror + ",Rotation:" + this.rot + ",Pos:" + this.pos + ",GenerationType:"
                + this.genType;
    }
}

package com.flemmli97.tenshilib.common.world.structure;

import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.PacketStructure;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class StructureMap extends WorldSavedData {

    private static final String id = "Structures";
    private Map<ResourceLocation, Set<StructureBase>> map = Maps.newHashMap();

    private Set<StructureBase> structureToGenerate = Sets.newHashSet();
    //Cache current structure
    private StructureBase currentStructure;

    public StructureMap(String id) {
        super(id);
    }

    public StructureMap() {
        this(id);
    }

    public static StructureMap get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        StructureMap data = (StructureMap) storage.getOrLoadData(StructureMap.class, id);
        if(data == null){
            data = new StructureMap();
            storage.setData(id, data);
        }
        return data;
    }

    public StructureBase getNearestStructure(ResourceLocation id, BlockPos pos, World world) {
        if(id == null){
            double dist = Double.MAX_VALUE;
            StructureBase nearest = null;
            for(Set<StructureBase> set : this.map.values()){
                for(StructureBase base : set){
                    double distTo = pos.distanceSq(base.getPos());
                    if(distTo < dist){
                        nearest = base;
                        dist = distTo;
                    }
                }
            }
            return nearest;
        }
        Set<StructureBase> set = this.map.get(id);
        if(set != null){
            double dist = Double.MAX_VALUE;
            StructureBase nearest = null;
            for(StructureBase base : set){
                double distTo = pos.distanceSq(base.getPos());
                if(distTo < dist){
                    nearest = base;
                    dist = distTo;
                }
            }
            return nearest;
        }
        return null;
    }

    public boolean isInside(ResourceLocation id, BlockPos pos) {
        if(id == null){
            for(Set<StructureBase> set : this.map.values()){
                for(StructureBase struc : set)
                    if(struc.isInside(pos)){
                        this.currentStructure = struc;
                        return true;
                    }
            }
            return false;
        }
        Set<StructureBase> set = this.map.get(id);
        if(set != null){
            for(StructureBase struc : set)
                if(struc.isInside(pos)){
                    return true;
                }
            return false;
        }
        return false;
    }

    @Nullable
    public StructureBase current(EntityPlayer player, boolean forced) {
        StructureBase base = this.currentStructure;
        this.current(player.getPosition());
        if(((this.currentStructure != null && base != this.currentStructure) || forced) && player instanceof EntityPlayerMP)
            PacketHandler.sendTo(new PacketStructure(this.currentStructure), (EntityPlayerMP) player);
        return this.currentStructure;
    }

    @Nullable
    public StructureBase current(BlockPos pos) {
        if(this.currentStructure != null && this.currentStructure.isInside(pos)){
            return this.currentStructure;
        }
        return this.isInside(null, pos) ? this.currentStructure : null;
    }

    @Nullable
    public ResourceLocation currentStructure(BlockPos pos) {
        return this.current(pos) != null ? this.currentStructure.getStructureId() : null;
    }

    public void initStructure(StructureBase base) {
        this.map.merge(base.getStructureId(), Sets.newHashSet(base), (old, val) -> {
            old.addAll(val);
            return old;
        });
        this.structureToGenerate.add(base);
        this.markDirty();
    }

    public void generate(World world, int chunkX, int chunkZ) {
        this.structureToGenerate.removeIf(structure -> structure.process(world, chunkX, chunkZ));
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nbt.getKeySet().forEach(key -> {
            NBTTagList tagList = nbt.getTagList(key, Constants.NBT.TAG_COMPOUND);
            tagList.forEach(compound -> this.map.merge(new ResourceLocation(key), Sets.newHashSet(new StructureBase((NBTTagCompound) compound)), (old, val) -> {
                old.addAll(val);
                return old;
            }));
        });
        NBTTagList list = nbt.getTagList("IncompleteStructures", Constants.NBT.TAG_COMPOUND);
        list.forEach(compound -> this.structureToGenerate.add(new StructureBase((NBTTagCompound) compound)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        this.map.forEach((key, value) -> {
            NBTTagList tagList = new NBTTagList();
            value.forEach(structure -> {
                tagList.appendTag(structure.writeToNBT(new NBTTagCompound()));
            });
            compound.setTag(key.toString(), tagList);
        });
        NBTTagList list = new NBTTagList();
        this.structureToGenerate.forEach(base -> list.appendTag(base.writeToNBT(new NBTTagCompound())));
        compound.setTag("IncompleteStructures", list);
        return compound;
    }
}

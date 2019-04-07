package com.flemmli97.tenshilib.common.entity;

import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityUtil {

	public static <T extends Entity> T findFromUUID(Class<T> clss, World world, UUID uuid)
	{
		return findFromUUID(clss, world, uuid, Predicates.alwaysTrue());
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> T findFromUUID(Class<T> clss, World world, UUID uuid, Predicate<T> pred)
	{
		if(world instanceof WorldServer)
		{
            Entity e = ((WorldServer)world).getEntityFromUuid(uuid);
            if(e!=null && clss.isAssignableFrom(e.getClass()) && pred.test((T) e))
            	return (T) e;
		}
		else
		{
			for(Entity e : world.loadedEntityList)
			{
				if(e.getUniqueID().equals(uuid) && clss.isAssignableFrom(e.getClass()) && pred.test((T) e))
					return (T) e;
			}
		}
		return null;
	}
}

package com.flemmli97.tenshilib.common.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityUtil {

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> T findFromUUID(Class<T> clss, World world, UUID uuid)
	{
		if(world instanceof WorldServer)
		{
            Entity e = ((WorldServer)world).getEntityFromUuid(uuid);
            if(e.getClass().isAssignableFrom(clss))
            	return (T) e;
		}
		else
		{
			for(Entity e : world.loadedEntityList)
			{
				if(e.getUniqueID().equals(uuid) && e.getClass().isAssignableFrom(clss))
					return (T) e;
			}
		}
		return null;
	}
}

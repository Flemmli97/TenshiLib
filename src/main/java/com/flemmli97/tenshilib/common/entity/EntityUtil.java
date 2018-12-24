package com.flemmli97.tenshilib.common.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityUtil {

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> T findFromUUID(Class<T> clss, World world, UUID uuid)
	{
		for(Entity e : world.loadedEntityList)
		{
			if(e.getUniqueID().equals(uuid) && e.getClass().isAssignableFrom(clss))
				return (T) e;
		}
		return null;
	}
}

package com.flemmli97.tenshilib.api.entity;

import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Bring it back
 */
public interface IOwnable<T extends Entity> {

    @Nullable
    UUID getOwnerUUID();

    @Nullable
    T getOwner();
}

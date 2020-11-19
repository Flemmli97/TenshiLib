package com.flemmli97.tenshilib.api.entity;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Bring it back
 */
public interface IOwnable<T extends LivingEntity> {

    @Nullable
    UUID ownerUUID();

    @Nullable
    T getOwner();
}

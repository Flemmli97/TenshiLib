package com.flemmli97.tenshilib.api.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public interface IBeamEntity {

    Vector3d startVec();

    Vector3d hitVec();

    int livingTickMax();

    void updateYawPitch();

    @Nullable
    LivingEntity getShooter();
}

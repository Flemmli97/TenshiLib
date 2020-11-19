package com.flemmli97.tenshilib.api.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

public interface IBeamEntity extends IOwnable<LivingEntity> {

    Vector3d startVec();

    Vector3d hitVec();

    int livingTickMax();

    void updateYawPitch();
}

package io.github.flemmli97.tenshilib.api.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.phys.Vec3;

public interface IBeamEntity extends OwnableEntity {

    Vec3 startVec();

    Vec3 hitVec();

    int livingTickMax();

    void updateYawPitch();

    float radius();

    default boolean firstPerson3d(Entity entity) {
        return this.getOwner() == entity;
    }
}

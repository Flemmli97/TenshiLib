package io.github.flemmli97.tenshilib.common.entity;

import io.github.flemmli97.tenshilib.common.entity.animated.AnimationDefinition;
import io.github.flemmli97.tenshilib.common.utils.math.OrientedBoundingBox;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface AOEAttackEntity {

    default OrientedBoundingBox prepareAttackBox(AnimationDefinition anim, @Nullable Entity target, double grow, boolean withDebug) {
        return this.prepareAttackBox(anim.id(), target, grow, withDebug);
    }

    /**
     * The bounding box from which if the target intersects it an attack is initiated
     *
     * @param anim      The animation to run for the attack
     * @param target    Current target of the entity
     * @param grow      A value to modify the AABB with
     * @param withDebug If true then the context in which this is called in should show a visual for the AABB
     */
    OrientedBoundingBox prepareAttackBox(String anim, @Nullable Entity target, double grow, boolean withDebug);
}

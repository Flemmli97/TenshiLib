package io.github.flemmli97.tenshilib.api.entity;

import io.github.flemmli97.tenshilib.common.utils.OrientedBoundingBox;
import net.minecraft.world.entity.LivingEntity;

public interface AoeAttackEntity {

    /**
     * The bounding box from which if the target intersects it an attack is initiated
     *
     * @param anim      The animation to run for the attack
     * @param target    Current target of the entity
     * @param grow      A value to modify the AABB with
     * @param withDebug If true then the context in which this is called in should show a visual for the AABB
     */
    OrientedBoundingBox prepareAttackBox(AnimatedAction anim, LivingEntity target, double grow, boolean withDebug);

}

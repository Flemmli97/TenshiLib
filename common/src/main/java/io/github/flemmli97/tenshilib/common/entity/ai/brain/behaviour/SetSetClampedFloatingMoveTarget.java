package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class SetSetClampedFloatingMoveTarget<E extends PathfinderMob> extends SetRandomWalkTarget<E> {

    private final Double min, max;

    public SetSetClampedFloatingMoveTarget(Double min) {
        this(min, null);
    }

    public SetSetClampedFloatingMoveTarget(Double min, Double max) {
        this.min = min;
        this.max = max;
        this.avoidWaterWhen(e -> false);
    }

    @Override
    protected @Nullable Vec3 getTargetPos(E entity) {
        Vec3 targetPos = super.getTargetPos(entity);
        if (targetPos != null) {
            LivingEntity target = BrainUtils.getTargetOfEntity(entity);
            if (target != null) {
                if (this.min != null && entity.getY() < (target.getY() - this.min)) {
                    targetPos = new Vec3(targetPos.x(), target.getY() - this.min, targetPos.z());
                } else if (this.max != null && entity.getY() > (target.getY() + this.max)) {
                    targetPos = new Vec3(targetPos.x(), target.getY() + this.max, targetPos.z());
                }
            }
        }
        return targetPos;
    }
}

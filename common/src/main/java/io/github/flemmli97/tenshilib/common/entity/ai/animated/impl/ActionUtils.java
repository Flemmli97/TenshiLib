package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.GoalAttackAction;
import net.minecraft.world.entity.PathfinderMob;

public class ActionUtils {

    public static <T extends PathfinderMob & IAnimated> GoalAttackAction.Condition<T> ranged(double range) {
        return (goal, target, previousAnim) -> goal.distanceToTargetSq < range * range;
    }

    public static <T extends PathfinderMob & IAnimated> GoalAttackAction.Condition<T> rangedVisible(double range) {
        return (goal, target, previousAnim) -> goal.canSee && goal.distanceToTargetSq < range * range;
    }

    /**
     * A GoalAttackAction that will fail and thus causes an idle process to run
     */
    public static <T extends PathfinderMob & IAnimated> GoalAttackAction<T> failedStart() {
        return new GoalAttackAction<>(null);
    }

    public static <T extends PathfinderMob & IAnimated> GoalAttackAction.Condition<T> chanced(FloatGetter<T> chance) {
        return (goal, target, previousAnim) -> goal.attacker.getRandom().nextFloat() < chance.get(goal.attacker);
    }

    public static <T extends PathfinderMob & IAnimated> GoalAttackAction.Condition<T> chanced(FloatGetter<T> chance, GoalAttackAction.Condition<T> other) {
        return (goal, target, previousAnim) -> {
            if (goal.attacker.getRandom().nextFloat() < chance.get(goal.attacker))
                return other.test(goal, target, previousAnim);
            return false;
        };
    }

    public interface FloatGetter<T> {

        float get(T value);
    }
}

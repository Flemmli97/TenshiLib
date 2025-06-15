package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class LeapInDirection<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = MemoryTest.builder(1).hasMemory(MemoryModuleType.ATTACK_TARGET);

    protected BiPredicate<E, LivingEntity> shouldLeap = (owner, target) -> true;
    protected BiFunction<E, LivingEntity, Vec3> horizontalLeapDirection = (owner, target) -> owner.getRandom().nextBoolean()
            ? createBackwardsVec(owner.position(), target.position()) :
            createSidewaysVec(owner.position(), target.position(), owner.getRandom().nextBoolean());
    protected BiFunction<E, LivingEntity, Double> verticalStrength = (owner, target) -> 0.2;
    protected BiFunction<E, LivingEntity, Double> leapStrength = (owner, target) -> 1d;

    public static Vec3 createSidewaysVec(Vec3 from, Vec3 to, boolean left) {
        Vec3 dir = to.subtract(from);
        dir = new Vec3(dir.x(), 0, dir.z()).normalize();
        return dir.yRot(left ? -90 : 90);
    }

    public static Vec3 createBackwardsVec(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from);
        return new Vec3(-dir.x(), 0, -dir.z()).normalize();
    }

    public LeapInDirection<E> shouldLeap(BiPredicate<E, LivingEntity> check) {
        this.shouldLeap = check;
        return this;
    }

    public LeapInDirection<E> horizontalDirection(BiFunction<E, LivingEntity, Vec3> direction) {
        this.horizontalLeapDirection = direction;
        return this;
    }

    public LeapInDirection<E> verticalStrength(double strength) {
        return this.verticalStrength((entity, target) -> strength);
    }

    public LeapInDirection<E> verticalStrength(BiFunction<E, LivingEntity, Double> strength) {
        this.verticalStrength = strength;
        return this;
    }

    public LeapInDirection<E> strength(double strength) {
        return this.strength((entity, target) -> strength);
    }

    public LeapInDirection<E> strength(BiFunction<E, LivingEntity, Double> strength) {
        this.verticalStrength = strength;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);
        if (this.shouldLeap.test(entity, target)) {
            Vec3 dir = this.horizontalLeapDirection.apply(entity, target);
            if (dir != null) {
                double strength = this.leapStrength.apply(entity, target);
                entity.setDeltaMovement(dir.x() * strength, this.verticalStrength.apply(entity, target), dir.z() * strength);
            }
        }
    }
}

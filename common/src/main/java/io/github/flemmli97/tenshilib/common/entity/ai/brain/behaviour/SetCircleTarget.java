package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.CircleData;
import io.github.flemmli97.tenshilib.common.registry.TenshilibMemoryModules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class SetCircleTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).usesMemories(TenshilibMemoryModules.CIRCLE_DATA.get());

    protected BiFunction<E, LivingEntity, PositionTracker> tracker = (owner, target) -> new BlockPosTracker(target.position());
    protected BiPredicate<E, LivingEntity> clockWise = (owner, target) -> owner.getRandom().nextBoolean();
    protected BiFunction<E, LivingEntity, Float> radius = (owner, target) -> 7f;
    protected BiFunction<E, LivingEntity, Float> speed = (owner, target) -> 1f;

    public SetCircleTarget<E> makeTracker(BiFunction<E, LivingEntity, PositionTracker> tracker) {
        this.tracker = tracker;
        return this;
    }

    public SetCircleTarget<E> clockWise(BiPredicate<E, LivingEntity> clockWise) {
        this.clockWise = clockWise;
        return this;
    }

    public SetCircleTarget<E> radius(float radius) {
        return this.radius((entity, target) -> radius);
    }

    public SetCircleTarget<E> radius(BiFunction<E, LivingEntity, Float> radius) {
        this.radius = radius;
        return this;
    }

    public SetCircleTarget<E> speed(float speed) {
        return this.speed((entity, target) -> speed);
    }

    public SetCircleTarget<E> speed(BiFunction<E, LivingEntity, Float> speed) {
        this.speed = speed;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);
        BrainUtils.setMemory(entity, TenshilibMemoryModules.CIRCLE_DATA.get(),
                new CircleData(this.tracker.apply(entity, target),
                        this.clockWise.test(entity, target),
                        this.radius.apply(entity, target),
                        this.speed.apply(entity, target)));
    }
}

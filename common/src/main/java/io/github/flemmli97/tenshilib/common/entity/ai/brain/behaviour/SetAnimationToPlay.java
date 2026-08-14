package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.AnimationPlayHolder;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.registry.TenshilibMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

public class SetAnimationToPlay<E extends Mob & AnimatedEntity> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(1).usesMemory(TenshilibMemoryModules.ANIMATION_TO_PLAY.get());

    private final List<AnimationPlayHolder<E>> animations;
    private List<AnimationPlayHolder<E>> selectable;

    private BiPredicate<String, E> filter;

    public SetAnimationToPlay(String... animations) {
        this.animations = Arrays.stream(animations).map(s -> new AnimationPlayHolder<E>(s)).toList();
    }

    @SafeVarargs
    public SetAnimationToPlay(AnimationPlayHolder<E>... animations) {
        this.animations = List.of(animations);
    }

    public SetAnimationToPlay<E> filter(BiPredicate<String, E> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if (entity.getAnimationHandler().hasAnimation())
            return false;
        this.selectable = this.filter == null ? this.animations : this.animations.stream().filter(h -> this.filter.test(h.animation(), entity)).toList();
        return !this.selectable.isEmpty();
    }

    @Override
    protected void start(E entity) {
        AnimationPlayHolder<E> selected = this.selectable.get(entity.getRandom().nextInt(this.selectable.size()));
        BrainUtils.setMemory(entity, TenshilibMemoryModules.ANIMATION_TO_PLAY.get(), selected);
    }
}

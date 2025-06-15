package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.AnimationPlayHolder;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.memory.MoreMemoryModules;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationHandler;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayAnimation<E extends Mob & AnimatedEntity> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(1).hasMemories(MoreMemoryModules.ANIMATION_TO_PLAY.get());

    private AnimationTickHandler<E> onAnimating;

    private List<AnimationPlayHolder.AnimationHolder> chainedAnimations;
    private int chainedIndex;
    private String currentPlaying;

    public PlayAnimation<E> withRunner(AnimationTickHandler<E> onAnimating) {
        this.onAnimating = onAnimating;
        return this;
    }

    public PlayAnimation<E> replaceRunner(AnimationTickHandler<E> onAnimating) {
        if (this.onAnimating == null && onAnimating != null)
            this.onAnimating = onAnimating;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        if (this.currentPlaying == null)
            return false;
        if (entity.getAnimationHandler().isCurrent(this.currentPlaying))
            return true;
        return this.chainedAnimations != null && this.chainedIndex < this.chainedAnimations.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void start(E entity) {
        BrainUtils.withMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get(), selected -> {
            this.currentPlaying = selected.animation();
            this.chainedAnimations = ((AnimationPlayHolder<E>) selected).get(entity);
            entity.getAnimationHandler().setAnimation(this.currentPlaying);
        });
        BrainUtils.clearMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get());
    }

    @Override
    protected void tick(E entity) {
        if (!entity.getAnimationHandler().isCurrent(this.currentPlaying)) {
            if (this.chainedAnimations != null && this.chainedIndex < this.chainedAnimations.size()) {
                AnimationPlayHolder.AnimationHolder selected = this.chainedAnimations.get(this.chainedIndex);
                this.chainedIndex++;
                AnimationHandler<?> handler = entity.getAnimationHandler();
                handler.setAnimation(handler.getAnimations().get(selected.animation()),
                        selected.transitionTime(), -1, selected.offset());
                this.currentPlaying = selected.animation();
            }
        }
        if (this.onAnimating != null)
            this.onAnimating.onTick(entity, BrainUtils.getTargetOfEntity(entity), entity.getAnimationHandler().getAnimation());
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get());
        this.chainedIndex = 0;
        this.currentPlaying = null;
        this.chainedAnimations = null;
    }

    public interface AnimationTickHandler<E> {

        void onTick(E entity, @Nullable LivingEntity target, AnimationState state);
    }
}

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

    private OnStart<E> onStartCallback;
    private OnContinue<E> onContinueCallback;

    private AnimationPlayHolder<E> selected;
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

    public PlayAnimation<E> withCallback(OnStart<E> onStartCallback) {
        this.onStartCallback = onStartCallback;
        return this;
    }

    public PlayAnimation<E> withCallback(OnContinue<E> onContinueCallback) {
        this.onContinueCallback = onContinueCallback;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void start(E entity) {
        BrainUtils.withMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get(), selected -> {
            this.selected = (AnimationPlayHolder<E>) selected;
            this.chainedIndex = -1;
            this.currentPlaying = this.selected.animation();
            entity.getAnimationHandler().setAnimation(this.currentPlaying);
            if (this.onStartCallback != null)
                this.onStartCallback.onStart(this.currentPlaying, entity);
        });
        BrainUtils.clearMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get());
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        if (this.currentPlaying == null)
            return false;
        if (entity.getAnimationHandler().isCurrent(this.currentPlaying))
            return true;
        return this.chainedIndex == -1 || (this.chainedAnimations != null && this.chainedIndex < this.chainedAnimations.size());
    }

    @Override
    protected void tick(E entity) {
        if (!entity.getAnimationHandler().isCurrent(this.currentPlaying)) {
            if (this.chainedIndex == -1) {
                this.chainedAnimations = this.selected.get(entity);
                this.chainedIndex = 0;
                if (this.chainedAnimations != null && this.onContinueCallback != null) {
                    this.onContinueCallback.onContinue(this.selected.animation(), this.chainedAnimations, entity);
                }
            }
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
        this.selected = null;
        this.chainedIndex = 0;
        this.currentPlaying = null;
        this.chainedAnimations = null;
    }

    public interface AnimationTickHandler<E> {

        void onTick(E entity, @Nullable LivingEntity target, AnimationState state);
    }

    public interface OnStart<E> {

        void onStart(String animation, E entity);
    }

    public interface OnContinue<E> {

        void onContinue(String start, @Nullable List<AnimationPlayHolder.AnimationHolder> chained, E entity);
    }
}

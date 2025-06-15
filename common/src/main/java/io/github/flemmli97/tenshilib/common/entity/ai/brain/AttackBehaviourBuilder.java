package io.github.flemmli97.tenshilib.common.entity.ai.brain;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour.PlayAnimation;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour.SetAnimationToPlay;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.AnimationPlayHolder;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.tslat.smartbrainlib.api.core.behaviour.AllApplicableBehaviours;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Eases creating of attack sequences
 */
public class AttackBehaviourBuilder<E extends Mob & AnimatedEntity> {

    private final List<Pair<ExtendedBehaviour<E>, Integer>> behaviors = new ArrayList<>();

    private PlayAnimation.AnimationTickHandler<E> universalHandler;

    public static <E extends Mob & AnimatedEntity> AttackBehaviourBuilder<E> create() {
        return new AttackBehaviourBuilder<>();
    }

    /**
     * Use a universal animation handler for all animation plays
     */
    public AttackBehaviourBuilder<E> universalHandler(PlayAnimation.AnimationTickHandler<E> animationTickHandler) {
        this.universalHandler = animationTickHandler;
        return this;
    }

    /**
     * Start building a new attack sequence with the given animations to select from
     */
    public SingleAttack start(String... animations) {
        return this.start(new SetAnimationToPlay<>(animations));
    }

    /**
     * Start building a new attack sequence with the given animations to select from
     */
    @SafeVarargs
    public final SingleAttack start(AnimationPlayHolder<E>... animations) {
        return this.start(new SetAnimationToPlay<>(animations));
    }

    /**
     * Start building a new attack sequence
     */
    public SingleAttack start(SetAnimationToPlay<E> behavior) {
        return new SingleAttack(behavior);
    }

    @SuppressWarnings("unchecked")
    public Behavior<E> build() {
        return new OneRandomBehaviour<>(this.behaviors.toArray(Pair[]::new));
    }

    public class SingleAttack {

        private final SetAnimationToPlay<E> setToPlay;
        private final List<ExtendedBehaviour<E>> preparations = new ArrayList<>();
        private Function<E, Integer> windupTime;

        private SingleAttack(SetAnimationToPlay<E> behavior) {
            this.setToPlay = behavior;
        }

        /**
         * Add a behaviour to run BEFORE the animation plays.
         */
        @SafeVarargs
        public final SingleAttack prepare(ExtendedBehaviour<E>... behaviors) {
            Collections.addAll(this.preparations, behaviors);
            return this;
        }

        /**
         * Using this makes preparation behaviours run in parallel instead of sequential.
         * This makes it possible to e.g. walk while playing the animation
         *
         * @param windupTime A delay before playing the animation so it doesn't constantly play
         */
        public SingleAttack parallel(Function<E, Integer> windupTime) {
            this.windupTime = windupTime;
            return this;
        }

        @SuppressWarnings("unchecked")
        private ExtendedBehaviour<E> seqOf(List<ExtendedBehaviour<E>> list) {
            if (list.isEmpty())
                return new Idle<E>().runFor(e -> 1);
            if (list.size() == 1)
                return list.getFirst();
            return new SequentialBehaviour<>(list.toArray(ExtendedBehaviour[]::new));
        }

        public AttackBehaviourBuilder<E> end() {
            return this.end(1);
        }

        public AttackBehaviourBuilder<E> end(int weight) {
            return this.end(weight, e -> true);
        }

        @SuppressWarnings("unchecked")
        public AttackBehaviourBuilder<E> end(int weight, Predicate<E> condition) {
            List<ExtendedBehaviour<E>> behaviours = new ArrayList<>();
            behaviours.add(this.setToPlay);
            ExtendedBehaviour<E> actuallyPlay = new PlayAnimation<E>().replaceRunner(AttackBehaviourBuilder.this.universalHandler);
            if (this.windupTime != null) {
                ExtendedBehaviour<E> preparation = this.seqOf(this.preparations);
                ExtendedBehaviour<E> attack = new SequentialBehaviour<>(new Idle<E>().runFor(this.windupTime),
                        actuallyPlay).startCondition(condition);
                behaviours.add(new AllApplicableBehaviours<>(preparation, attack));
            } else {
                behaviours.addAll(this.preparations);
                behaviours.add(actuallyPlay);
            }
            ExtendedBehaviour<E> attackBehaviour = new SequentialBehaviour<E>(behaviours.toArray(ExtendedBehaviour[]::new))
                    .startCondition(condition);
            AttackBehaviourBuilder.this.behaviors.add(new Pair<>(attackBehaviour, weight));
            return AttackBehaviourBuilder.this;
        }
    }
}

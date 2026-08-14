package io.github.flemmli97.tenshilib.client.model;

import io.github.flemmli97.tenshilib.client.model.animation.Animation;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import org.jetbrains.annotations.Nullable;

public interface ExtendedModel {

    ModelPartsContainer getModel();

    default void onPlayAnimation(@Nullable AnimationState state, Animation animation, float tick, VariableMap variables) {
        if (animation.variables().contains("query.anim_time")) {
            variables.setVariable("query.anim_time", tick * 0.05);
        }
        if (animation.variables().contains("tick")) {
            variables.setVariable("tick", tick * 0.05);
        }
        if (animation.variables().contains("anim_time")) {
            variables.setVariable("anim_time", tick * 0.05);
        }
    }
}

package io.github.flemmli97.tenshilib.common.entity.animated;

import java.util.Collection;
import java.util.Collections;

public class DefaultedAnimationContainer extends AnimationDefinitionContainer {

    private final AnimationDefinitionContainer defaulted;
    private final AnimationDefinitionContainer wrapped;

    public DefaultedAnimationContainer(AnimationDefinitionContainer defaulted, AnimationDefinitionContainer wrapped) {
        super(Collections.emptyMap());
        this.defaulted = defaulted;
        this.wrapped = wrapped;
    }

    @Override
    public AnimationDefinition get(String id) {
        AnimationDefinition def = this.wrapped.get(id);
        return def != null ? def : this.defaulted.get(id);
    }

    @Override
    public Collection<String> all() {
        return this.defaulted.all();
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser.impl;

import com.google.common.collect.ImmutableSet;
import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;

import java.util.Set;

public abstract class FunctionalValue implements ExpValue {

    private final ExpValue[] values;

    private final Set<String> variables;

    protected FunctionalValue(ExpValue... values) {
        this.values = values;
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (ExpValue value : values) {
            builder.add(value.toString());
        }
        this.variables = builder.build();
    }

    public ExpValue[] values() {
        return this.values;
    }

    @Override
    public Set<String> variables() {
        return this.variables;
    }
}

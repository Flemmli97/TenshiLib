package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class Min extends FunctionalValue {

    private final ExpValue first;
    private final ExpValue second;

    public Min(ExpValue first, ExpValue second) {
        super(first, second);
        this.first = first;
        this.second = second;
    }

    @Override
    public double get(VariableMap vars) {
        return Math.min(this.first.get(vars), this.second.get(vars));
    }

    @Override
    public String toString() {
        return String.format("min(%s, %s)", this.first, this.second);
    }
}

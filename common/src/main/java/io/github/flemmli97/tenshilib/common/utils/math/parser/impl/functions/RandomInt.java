package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class RandomInt extends FunctionalValue {

    private final ExpValue min;
    private final ExpValue max;

    public RandomInt(ExpValue min, ExpValue max) {
        super(min, max);
        this.min = min;
        this.max = max;
    }

    @Override
    public double get(VariableMap vars) {
        return vars.random().nextInt((int) this.min.get(vars), (int) this.max.get(vars));
    }

    @Override
    public String toString() {
        return String.format("randInt(%s, %s)", this.min, this.max);
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class And extends FunctionalValue {

    private final ExpValue first;
    private final ExpValue second;

    public And(ExpValue first, ExpValue second) {
        super(first, second);
        this.first = first;
        this.second = second;
    }

    @Override
    public double get(VariableMap vars) {
        return this.first.asBool(vars) && this.second.asBool(vars) ? 1 : 0;
    }

    @Override
    public String toString() {
        return String.format("%s&&%s", this.first, this.second);
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record GreaterThan(ExpValue first, ExpValue second) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return this.first.get(vars) >= this.second.get(vars) ? 1 : 0;
    }

    @Override
    public String toString() {
        return String.format("%s>=%s", this.first, this.second);
    }
}

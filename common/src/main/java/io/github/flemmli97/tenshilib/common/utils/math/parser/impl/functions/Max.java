package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record Max(ExpValue first, ExpValue second) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Math.max(this.first.get(vars), this.second.get(vars));
    }

    @Override
    public String toString() {
        return String.format("max(%s, %s)", this.first, this.second);
    }
}

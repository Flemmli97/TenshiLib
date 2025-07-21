package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record Power(ExpValue base, ExpValue exponent) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Math.pow(this.base.get(vars), this.exponent.get(vars));
    }

    @Override
    public String toString() {
        return String.format("(%s^%s)", this.base, this.exponent);
    }
}

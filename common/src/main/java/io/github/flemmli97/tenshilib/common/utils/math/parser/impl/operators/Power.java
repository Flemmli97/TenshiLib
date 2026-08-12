package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class Power extends FunctionalValue {

    private final ExpValue base;
    private final ExpValue exponent;

    public Power(ExpValue base, ExpValue exponent) {
        super(base, exponent);
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public double get(VariableMap vars) {
        return Math.pow(this.base.get(vars), this.exponent.get(vars));
    }

    @Override
    public String toString() {
        return String.format("(%s^%s)", this.base, this.exponent);
    }
}

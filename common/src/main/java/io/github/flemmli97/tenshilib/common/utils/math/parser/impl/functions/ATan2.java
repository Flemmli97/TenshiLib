package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record ATan2(ExpValue a, ExpValue b) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Math.atan2(Constants.DEG_TO_RAD * this.a.get(vars), Constants.DEG_TO_RAD * this.b.get(vars));
    }

    @Override
    public String toString() {
        return String.format("atan(%s, %s)", this.a, this.b);
    }
}

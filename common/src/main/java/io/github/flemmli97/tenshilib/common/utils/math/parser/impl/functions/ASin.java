package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record ASin(ExpValue value) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Math.asin(Constants.DEG_TO_RAD * this.value.get(vars));
    }

    @Override
    public String toString() {
        return String.format("asin(%s)", this.value);
    }
}

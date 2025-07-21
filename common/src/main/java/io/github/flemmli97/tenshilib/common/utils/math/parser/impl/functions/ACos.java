package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record ACos(ExpValue value) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Math.acos(Constants.DEG_TO_RAD * this.value.get(vars));
    }

    @Override
    public String toString() {
        return String.format("acos(%s)", this.value);
    }
}

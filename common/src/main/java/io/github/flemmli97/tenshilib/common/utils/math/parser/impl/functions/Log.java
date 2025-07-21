package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record Log(ExpValue value) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Math.log(this.value.get(vars));
    }

    @Override
    public String toString() {
        return String.format("ln(%s)", this.value);
    }
}

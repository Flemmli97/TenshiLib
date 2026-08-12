package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public class Floor extends FunctionalValue {

    private final ExpValue value;

    public Floor(ExpValue value) {
        super(value);
        this.value = value;
    }

    @Override
    public double get(VariableMap vars) {
        return Math.floor(this.value.get(vars));
    }

    @Override
    public String toString() {
        return String.format("floor(%s)", this.value);
    }
}

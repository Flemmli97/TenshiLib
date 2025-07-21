package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record Modulo(ExpValue value, ExpValue modulus) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return this.value.get(vars) % this.modulus.get(vars);
    }

    @Override
    public String toString() {
        return String.format("(%s%%%s)", this.value, this.modulus);
    }
}

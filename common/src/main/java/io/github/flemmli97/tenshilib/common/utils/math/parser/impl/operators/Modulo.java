package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class Modulo extends FunctionalValue {

    private final ExpValue value;
    private final ExpValue modulus;

    public Modulo(ExpValue value, ExpValue modulus) {
        super(value, modulus);
        this.value = value;
        this.modulus = modulus;
    }

    @Override
    public double get(VariableMap vars) {
        return this.value.get(vars) % this.modulus.get(vars);
    }

    @Override
    public String toString() {
        return String.format("(%s%%%s)", this.value, this.modulus);
    }
}

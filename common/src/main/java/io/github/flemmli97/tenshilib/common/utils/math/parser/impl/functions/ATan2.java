package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class ATan2 extends FunctionalValue {

    private final ExpValue a;
    private final ExpValue b;

    public ATan2(ExpValue a, ExpValue b) {
        super(a, b);
        this.a = a;
        this.b = b;
    }

    @Override
    public double get(VariableMap vars) {
        return Math.atan2(Constants.DEG_TO_RAD * this.a.get(vars), Constants.DEG_TO_RAD * this.b.get(vars));
    }

    @Override
    public String toString() {
        return String.format("atan(%s, %s)", this.a, this.b);
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public record Random(ExpValue min, ExpValue max) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        double minVal = this.min.get(vars);
        double maxVal = this.max.get(vars);
        return vars.random().nextDouble() * (maxVal - minVal) + minVal;
    }

    @Override
    public String toString() {
        return String.format("rand(%s, %s)", this.min, this.max);
    }
}

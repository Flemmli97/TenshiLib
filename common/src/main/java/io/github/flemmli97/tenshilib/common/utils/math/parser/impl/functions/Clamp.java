package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;
import net.minecraft.util.Mth;

public final class Clamp extends FunctionalValue {

    private final ExpValue value;
    private final ExpValue min;
    private final ExpValue max;

    public Clamp(ExpValue value, ExpValue min, ExpValue max) {
        super(value, min, max);
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @Override
    public double get(VariableMap vars) {
        return Mth.clamp(this.value.get(vars), this.min.get(vars), this.max.get(vars));
    }

    @Override
    public String toString() {
        return String.format("clamp(%s, %s, %s)", this.value, this.min, this.max);
    }
}

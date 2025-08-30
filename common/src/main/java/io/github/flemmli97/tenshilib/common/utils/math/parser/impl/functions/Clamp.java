package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.util.Mth;

public record Clamp(ExpValue value, ExpValue min, ExpValue max) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return Mth.clamp(this.value.get(vars), this.min.get(vars), this.max.get(vars));
    }

    @Override
    public String toString() {
        return String.format("clamp(%s, %s, %s)", this.value, this.min, this.max);
    }
}

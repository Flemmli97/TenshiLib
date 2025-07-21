package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import org.jetbrains.annotations.Nullable;

public record Condition(ExpValue condition, ExpValue then, @Nullable ExpValue otherwise) implements ExpValue {

    public Condition(ExpValue then, ExpValue condition) {
        this(condition, then, null);
    }

    @Override
    public double get(VariableMap vars) {
        return this.condition.asBool(vars) ? this.then.get(vars) : this.otherwise == null ? 0 : this.otherwise().get(vars);
    }

    @Override
    public String toString() {
        if (this.otherwise == null)
            return String.format("%s?:%s", this.condition, this.then);
        return String.format("%s?%s:%s", this.condition, this.then, this.otherwise);
    }
}

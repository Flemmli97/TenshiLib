package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.FunctionalValue;

public final class Condition extends FunctionalValue {

    private final ExpValue condition;
    private final ExpValue then;
    private final ExpValue otherwise;

    public Condition(ExpValue then, ExpValue condition) {
        this(condition, then, null);
    }

    public Condition(ExpValue condition, ExpValue then, ExpValue otherwise) {
        super(condition, then, otherwise);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public double get(VariableMap vars) {
        return this.condition.asBool(vars) ? this.then.get(vars) : this.otherwise == null ? 0 : this.otherwise.get(vars);
    }

    @Override
    public String toString() {
        if (this.otherwise == null)
            return String.format("%s?:%s", this.condition, this.then);
        return String.format("%s?%s:%s", this.condition, this.then, this.otherwise);
    }
}

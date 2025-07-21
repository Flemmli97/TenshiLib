package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.BuiltinValues;

public record Multiplication(ExpValue first, ExpValue second) implements ExpValue {

    @Override
    public double get(VariableMap vars) {
        return this.first.get(vars) * this.second.get(vars);
    }

    @Override
    public String toString() {
        String f = this.first instanceof BuiltinValues.ConstantValue || this.first instanceof BuiltinValues.VariableValue ? this.first.toString() : String.format("(%s)", this.first);
        String s = this.second instanceof BuiltinValues.ConstantValue || this.second instanceof BuiltinValues.VariableValue ? this.second.toString() : String.format("(%s)", this.second);
        return String.format("%s*%s", f, s);
    }
}

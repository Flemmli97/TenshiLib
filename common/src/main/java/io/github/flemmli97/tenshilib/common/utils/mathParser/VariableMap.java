package io.github.flemmli97.tenshilib.common.utils.mathParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class VariableMap {

    private final Map<String, DoubleSupplier> variables = new HashMap<>();

    public VariableMap setVariable(String variable, DoubleSupplier value) {
        this.variables.put(variable, value);
        return this;
    }

    public double getValue(String variable) {
        DoubleSupplier sup = this.variables.get(variable);
        return sup != null ? sup.getAsDouble() : 0;
    }
}

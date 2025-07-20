package io.github.flemmli97.tenshilib.common.utils.math.parser;

public interface ExpValue {

    ExpValue DEFAULT = new BuiltinValues.ConstantValue(0);

    /**
     * Computes the value using the given variables
     */
    double get(VariableMap variables);

    default float asFloat(VariableMap variables) {
        return (float) this.get(variables);
    }
}

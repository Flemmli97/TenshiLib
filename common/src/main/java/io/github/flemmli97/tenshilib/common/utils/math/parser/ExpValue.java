package io.github.flemmli97.tenshilib.common.utils.math.parser;

import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.BuiltinValues;

public interface ExpValue {

    ExpValue DEFAULT = new BuiltinValues.ConstantValue(0);

    /**
     * Computes the value using the given variables
     */
    double get(VariableMap variables);

    /**
     * Computes the boolean value using the given variables
     * Internally all booleans are represented as 1 (true) and 0 (false)
     */
    default boolean asBool(VariableMap variables) {
        return this.get(variables) == 1;
    }

    default float asFloat(VariableMap variables) {
        return (float) this.get(variables);
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser;

public interface ExpValue {

    double get(VariableMap variables);

    default float asFloat(VariableMap variables) {
        return (float) this.get(variables);
    }
}

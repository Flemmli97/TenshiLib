package io.github.flemmli97.tenshilib.common.utils.mathParser;

public interface ExpValue {

    double get(VariableMap variables);

    default float asFloat(VariableMap variables) {
        return (float) this.get(variables);
    }
}

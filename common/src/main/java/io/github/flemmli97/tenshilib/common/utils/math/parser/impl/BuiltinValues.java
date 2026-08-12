package io.github.flemmli97.tenshilib.common.utils.math.parser.impl;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

import java.util.Set;

public class BuiltinValues {

    public record ConstantValue(double constant) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return this.constant;
        }

        @Override
        public String toString() {
            return this.constant + "";
        }
    }

    public record NegativeValue(ExpValue val) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return -this.val.get(vars);
        }

        @Override
        public Set<String> variables() {
            return this.val.variables();
        }

        @Override
        public String toString() {
            return "-" + this.val;
        }
    }

    public record VariableValue(String variable) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return vars.getValue(this.variable);
        }

        @Override
        public Set<String> variables() {
            return Set.of(this.variable);
        }

        @Override
        public String toString() {
            return this.variable;
        }
    }

    public record NegatedValue(ExpValue val) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return this.val.asBool(vars) ? 0 : 1;
        }

        @Override
        public Set<String> variables() {
            return this.val.variables();
        }

        @Override
        public String toString() {
            return "!" + this.val;
        }
    }
}

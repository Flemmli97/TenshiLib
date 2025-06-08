package io.github.flemmli97.tenshilib.common.utils.mathParser;

public class BuiltinFunctions {

    public static final double DEG_TO_RAD = Math.PI / 180;

    public record Sin(ExpValue value) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.sin(DEG_TO_RAD * this.value.get(vars));
        }

        @Override
        public String toString() {
            return String.format("sin(%s)", this.value);
        }
    }

    public record Cos(ExpValue value) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.cos(DEG_TO_RAD * this.value.get(vars));
        }

        @Override
        public String toString() {
            return String.format("cos(%s)", this.value);
        }
    }

    public record Abs(ExpValue value) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.abs(this.value.get(vars));
        }

        @Override
        public String toString() {
            return String.format("abs(%s)", this.value);
        }
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser;

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

    public record Min(ExpValue first, ExpValue second) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.min(this.first.get(vars), this.second.get(vars));
        }

        @Override
        public String toString() {
            return String.format("min(%s, %s)", this.first, this.second);
        }
    }

    public record Max(ExpValue first, ExpValue second) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.max(this.first.get(vars), this.second.get(vars));
        }

        @Override
        public String toString() {
            return String.format("max(%s, %s)", this.first, this.second);
        }
    }

    public record Sqrt(ExpValue value) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.sqrt(this.value.get(vars));
        }

        @Override
        public String toString() {
            return String.format("sqrt(%s)", this.value);
        }
    }

    public record Log(ExpValue value) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.log(this.value.get(vars));
        }

        @Override
        public String toString() {
            return String.format("log(%s)", this.value);
        }
    }

    public record Power(ExpValue base, ExpValue exponent) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return Math.pow(this.base.get(vars), this.exponent.get(vars));
        }

        @Override
        public String toString() {
            return String.format("pow(%s, %s)", this.base, this.exponent);
        }
    }

    public record Random(ExpValue min, ExpValue max) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            double minVal = this.min.get(vars);
            double maxVal = this.max.get(vars);
            return vars.random().nextDouble() * (maxVal - minVal) + minVal;
        }

        @Override
        public String toString() {
            return String.format("rand(%s, %s)", this.min, this.max);
        }
    }

    public record RandomInt(ExpValue min, ExpValue max) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return vars.random().nextInt((int) this.min.get(vars), (int) this.max.get(vars));
        }

        @Override
        public String toString() {
            return String.format("randInt(%s, %s)", this.min, this.max);
        }
    }
}

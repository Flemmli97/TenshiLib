package io.github.flemmli97.tenshilib.common.utils.math.parser;

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
        public String toString() {
            return this.variable;
        }
    }

    public record Addition(ExpValue first, ExpValue second) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return this.first.get(vars) + this.second.get(vars);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof VariableValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof VariableValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s+%s", f, s);
        }
    }

    public record Substraction(ExpValue first, ExpValue second) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return this.first.get(vars) - this.second.get(vars);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof VariableValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof VariableValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s-%s", f, s);
        }
    }

    public record Multiplication(ExpValue first, ExpValue second) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return this.first.get(vars) * this.second.get(vars);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof VariableValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof VariableValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s*%s", f, s);
        }
    }

    public record Division(ExpValue first, ExpValue second) implements ExpValue {

        @Override
        public double get(VariableMap vars) {
            return this.first.get(vars) / this.second.get(vars);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof VariableValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof VariableValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s/%s", f, s);
        }
    }
}

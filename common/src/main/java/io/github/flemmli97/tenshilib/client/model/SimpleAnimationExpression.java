package io.github.flemmli97.tenshilib.client.model;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Simple molang animation parser cause yes.
 * Made it because I didn't want to use another library and also as a way of learning.
 * Can parse simple math expressions with sin and cos support and a single variable
 * of name "time" or "query.time".
 * sin and cos both use deg
 * Example parsable expression:
 * 1 -> 1
 * 1+2+3+4+5 -> 15
 * 1+2+3*4 -> 15
 * math.sin(time*5)
 */
public class SimpleAnimationExpression {

    private static final String REGEX_SPLIT = "(?<=%1$s)|(?=%1$s)";

    private static final String DELIMITER = "[+\\-\\*/()]";

    private static final String[] DELIMS = {
            "[+\\-\\*/()]",
            "(math\\.sin)",
            "(math\\.cos)",
            "(time)",
            "(query.time)"
    };

    private static Type type(String s) {
        return switch (s) {
            case "+" -> Type.ADD;
            case "-" -> Type.SUB;
            case "*" -> Type.MULT;
            case "/" -> Type.DIV;
            case "(" -> Type.BRACKETOPEN;
            case ")" -> Type.BRACKETCLOSE;
            case "math.sin" -> Type.SIN;
            case "math.cos" -> Type.COS;
            case "time", "query.time" -> Type.VAR;
            default -> Type.NUMBER;
        };
    }

    private static final String DEL_COMP = String.join("|", DELIMS);

    public static Value of(String exp) {
        exp = exp.replace(" ", "");
        try {
            float f = Float.parseFloat(exp);
            return new ConstantValue(f);
        } catch (NumberFormatException e) {
            try {
                return ofSplit(exp.split(String.format(REGEX_SPLIT, DEL_COMP)));
            } catch (NumberFormatException ignored) {
            }
        }
        TenshiLib.LOGGER.error("Couldn't parse expression " + exp);
        return new ConstantValue(0);
    }

    private static Value ofSplit(String[] sub) {
        Stack<Value> consts = new Stack<>();
        Stack<Type> ops = new Stack<>();

        List<String> bracketSS = new ArrayList<>();

        int bracket = 0;
        for (String s : sub) {
            Type type = type(s);
            if (type == Type.BRACKETOPEN) {
                bracket++;
            }
            if (type == Type.BRACKETCLOSE) {
                bracket--;
                if (bracket <= 0) {
                    bracketSS.remove(0);
                    Value b = ofSplit(bracketSS.toArray(new String[0]));
                    consts.push(b);
                    bracketSS.clear();
                    continue;
                }
            }
            if (bracket > 0) {
                bracketSS.add(s);
                continue;
            }
            if (type == Type.NUMBER)
                consts.push(new ConstantValue(Float.parseFloat(s)));
            else if (type == Type.VAR)
                consts.push(new TickValue());
            else if (!ops.isEmpty() && ops.peek().priority > type.priority) {
                Value v = null;
                while (!ops.empty() && ops.peek().priority > type.priority) {
                    Type t = ops.pop();
                    v = read(t, consts, v);
                }
                ops.push(type);
                consts.push(v);
            } else
                ops.push(type);
        }
        Value v = null;
        while (!ops.empty()) {
            Type t = ops.pop();
            v = read(t, consts, v);
        }
        if (v == null && !consts.empty())
            v = consts.pop();
        return v;
    }

    private static Value read(Type t, Stack<Value> stack, Value prev) {
        switch (t) {
            case ADD -> {
                Value sec = prev == null ? stack.pop() : prev;
                Value first = stack.pop();
                return new Addition(first, sec);
            }
            case SUB -> {
                if (stack.empty()) {
                    if (prev instanceof BiValue bi) {
                        return bi.negateFirst();
                    }
                    if (prev instanceof ConstantValue consts)
                        return consts.negate();
                    return new NegValue(prev);
                }
                Value sec = prev == null ? stack.pop() : prev;
                Value first = stack.pop();
                // Cause our whole thing is right assosiative we need to inverse the 2. part for sub
                return new Substraction(first, sec instanceof BiValue b ? b.negateSecond() : sec);
            }
            case MULT -> {
                Value sec = prev == null ? stack.pop() : prev;
                Value first = stack.pop();
                return new Multiplication(first, sec);
            }
            case DIV -> {
                Value sec = prev == null ? stack.pop() : prev;
                Value first = stack.pop();
                return new Division(first, sec);
            }
            case SIN -> {
                return new Sin(prev != null ? prev : stack.pop());
            }
            case COS -> {
                return new Cos(prev != null ? prev : stack.pop());
            }
        }
        return new ConstantValue(0);
    }

    interface Value {
        float get(float time);
    }

    interface BiValue extends Value {

        Value getFirst();

        Value getSecond();

        Value negateFirst();

        Value negateSecond();

    }

    record ConstantValue(float constant) implements Value {

        @Override
        public float get(float time) {
            return this.constant;
        }

        public ConstantValue negate() {
            return new ConstantValue(-this.constant);
        }

        @Override
        public String toString() {
            return this.constant + "";
        }
    }

    record NegValue(Value val) implements Value {

        @Override
        public float get(float time) {
            return -this.val.get(time);
        }

        @Override
        public String toString() {
            return "-" + this.val;
        }
    }

    record TickValue() implements Value {

        @Override
        public float get(float time) {
            return time;
        }

        @Override
        public String toString() {
            return "time";
        }
    }

    record Addition(Value first, Value second) implements BiValue {

        @Override
        public float get(float time) {
            return this.first.get(time) + this.second.get(time);
        }

        @Override
        public Value getFirst() {
            return this.first;
        }

        @Override
        public Value getSecond() {
            return this.second;
        }

        @Override
        public Value negateFirst() {
            if (this.first instanceof ConstantValue c)
                return new Addition(c.negate(), this.second);
            return new Addition(new NegValue(this.first), this.second);
        }

        @Override
        public Value negateSecond() {
            if (this.second instanceof ConstantValue c)
                return new Addition(this.first, c.negate());
            return new Addition(this.first, new NegValue(this.second));
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof TickValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof TickValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s+%s", f, s);
        }
    }

    record Substraction(Value first, Value second) implements BiValue {

        @Override
        public float get(float time) {
            return this.first.get(time) - this.second.get(time);
        }

        @Override
        public Value getFirst() {
            return this.first;
        }

        @Override
        public Value getSecond() {
            return this.second;
        }

        @Override
        public Value negateFirst() {
            if (this.first instanceof ConstantValue c)
                return new Substraction(c.negate(), this.second);
            return new Substraction(new NegValue(this.first), this.second);
        }

        @Override
        public Value negateSecond() {
            if (this.second instanceof ConstantValue c)
                return new Substraction(this.first, c.negate());
            return new Substraction(this.first, new NegValue(this.second));
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof TickValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof TickValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s-%s", f, s);
        }
    }

    record Multiplication(Value first, Value second) implements BiValue {

        @Override
        public float get(float time) {
            return this.first.get(time) * this.second.get(time);
        }

        @Override
        public Value getFirst() {
            return this.first;
        }

        @Override
        public Value getSecond() {
            return this.second;
        }

        @Override
        public Value negateFirst() {
            if (this.first instanceof ConstantValue c)
                return new Multiplication(c.negate(), this.second);
            return new Multiplication(new NegValue(this.first), this.second);
        }

        @Override
        public Value negateSecond() {
            if (this.second instanceof ConstantValue c)
                return new Multiplication(this.first, c.negate());
            return new Multiplication(this.first, new NegValue(this.second));
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof TickValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof TickValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s*%s", f, s);
        }
    }

    record Division(Value first, Value second) implements BiValue {

        @Override
        public float get(float time) {
            return this.first.get(time) / this.second.get(time);
        }

        @Override
        public Value getFirst() {
            return this.first;
        }

        @Override
        public Value getSecond() {
            return this.second;
        }

        @Override
        public Value negateFirst() {
            if (this.first instanceof ConstantValue c)
                return new Division(c.negate(), this.second);
            return new Division(new NegValue(this.first), this.second);
        }

        @Override
        public Value negateSecond() {
            if (this.second instanceof ConstantValue c)
                return new Division(this.first, c.negate());
            return new Division(this.first, new NegValue(this.second));
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue || this.first instanceof TickValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue || this.second instanceof TickValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s/%s", f, s);
        }
    }

    record Sin(Value value) implements Value {

        @Override
        public float get(float time) {
            return Mth.sin(Mth.DEG_TO_RAD * this.value.get(time));
        }

        @Override
        public String toString() {
            return String.format("sin(%s)", this.value);
        }
    }

    record Cos(Value value) implements Value {

        @Override
        public float get(float time) {
            return Mth.cos(Mth.DEG_TO_RAD * this.value.get(time));
        }

        @Override
        public String toString() {
            return String.format("cos(%s)", this.value);
        }
    }

    enum Type {
        NUMBER(0),
        MULT(1),
        DIV(1),
        ADD(0),
        SUB(0),
        SIN(2),
        COS(2),
        BRACKETOPEN(2),
        BRACKETCLOSE(2),
        VAR(0);

        int priority;

        Type(int priority) {
            this.priority = priority;
        }
    }
}

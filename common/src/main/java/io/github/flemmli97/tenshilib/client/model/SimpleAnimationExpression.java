package io.github.flemmli97.tenshilib.client.model;

import io.github.flemmli97.tenshilib.TenshiLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Simple molang animation parser cause yes.
 * Made it cause I didn't want to use another library and also as a way of learning.
 * Can parse simple math expressions with sin and cos support and a single variable
 * of name "time" or "query.time"
 * Example parsable expression:
 * 1+2+3+4+5 -> 15
 * 1+2+3*4 -> 15
 * math.sin(time*5)
 */
public class SimpleAnimationExpression {

    private static final String REGEX_SPLIT = "(?<=%1$s)|(?=%1$s)";

    private static final String DELIMITER = "[+\\-\\*/()]";

    private static final String[] delims = {
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

    private static final String delComp = String.join("|", delims);

    private static final Pattern regex = Pattern.compile(String.format(REGEX_SPLIT, delComp));

    public static Value of(String exp) {
        exp = exp.replace(" ", "");
        try {
            float f = Float.parseFloat(exp);
            return new ConstantValue(f);
        } catch (NumberFormatException e) {
            try {
                return ofSplit(exp.split(String.format(REGEX_SPLIT, delComp)));
            } catch (NumberFormatException ignored) {
            }
        }
        TenshiLib.logger.error("Couldn't parse expression " + exp);
        return new ConstantValue(0);
    }

    private static Value ofSplit(String[] sub) {
        Stack<Value> consts = new Stack<>();
        Stack<Type> ops = new Stack<>();

        List<String> bracketSS = new ArrayList<>();

        int bracket = 0;
        for (int i = 0; i < sub.length; i++) {
            Type type = type(sub[i]);
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
                bracketSS.add(sub[i]);
                continue;
            }
            if (type == Type.NUMBER)
                consts.push(new ConstantValue(Float.parseFloat(sub[i])));
            else if (type == Type.VAR)
                consts.push(new TickValue());
            else if (!ops.isEmpty() && ops.peek().priority > type.priority) {
                Value v = null;
                while (!ops.empty() && ops.peek().priority > type.priority) {
                    Type t = ops.pop();
                    v = t(t, consts, v);
                }
                ops.push(type);
                consts.push(v);
            } else
                ops.push(type);
        }
        Value v = null;
        while (!ops.empty()) {
            Type t = ops.pop();
            v = t(t, consts, v);
        }
        if (v == null && !consts.empty())
            v = consts.pop();
        return v;
    }

    private static Value t(Type t, Stack<Value> stack, Value prev) {
        switch (t) {
            case ADD -> {
                Value sec = stack.pop();
                Value first = prev != null ? prev : stack.pop();
                return new Addition(first, sec);
            }
            case SUB -> {
                Value sec = stack.pop();
                Value first = prev != null ? prev : stack.pop();
                return new Substraction(first, sec);
            }
            case MULT -> {
                Value sec = stack.pop();
                Value first = prev != null ? prev : stack.pop();
                return new Multiplication(first, sec);
            }
            case DIV -> {
                Value sec = stack.pop();
                Value first = prev != null ? prev : stack.pop();
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

    record ConstantValue(float constant) implements Value {

        @Override
        public float get(float time) {
            return this.constant;
        }

        @Override
        public String toString() {
            return this.constant + "";
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

    record Addition(Value first, Value second) implements Value {

        @Override
        public float get(float time) {
            return this.first.get(time) + this.second.get(time);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s+%s", f, s);
        }
    }

    record Substraction(Value first, Value second) implements Value {

        @Override
        public float get(float time) {
            return this.first.get(time) - this.second.get(time);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s-%s", f, s);
        }
    }

    record Multiplication(Value first, Value second) implements Value {

        @Override
        public float get(float time) {
            return this.first.get(time) * this.second.get(time);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s*%s", f, s);
        }
    }

    record Division(Value first, Value second) implements Value {

        @Override
        public float get(float time) {
            return this.first.get(time) / this.second.get(time);
        }

        @Override
        public String toString() {
            String f = this.first instanceof ConstantValue ? this.first.toString() : String.format("(%s)", this.first);
            String s = this.second instanceof ConstantValue ? this.second.toString() : String.format("(%s)", this.second);
            return String.format("%s/%s", f, s);
        }
    }

    record Sin(Value value) implements Value {

        @Override
        public float get(float time) {
            return (float) Math.sin(this.value.get(time));
        }

        @Override
        public String toString() {
            return String.format("sin(%s)", this.value);
        }
    }

    record Cos(Value value) implements Value {

        @Override
        public float get(float time) {
            return (float) Math.cos(this.value.get(time));
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

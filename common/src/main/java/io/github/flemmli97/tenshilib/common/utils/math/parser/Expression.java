package io.github.flemmli97.tenshilib.common.utils.math.parser;

import org.jetbrains.annotations.TestOnly;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Math parser able to create a math expression from a string.
 * Just call {@link Expression#of}
 */
public class Expression {

    private static final String NUMBER = "\\d+(?:\\.\\d+)?";
    private static final String VARIABLE = "(?<!\\.)[A-z][A-z._]*";
    private static final String OPERATOR = "[*/+\\-%()]";

    private static final Pattern PATTERN = Pattern.compile(String.format("(?:%1$s)|(?:%2$s)|(?:%3$s)|,", NUMBER, VARIABLE, OPERATOR));

    private static String variableEquivalent(String variable) {
        if (variable.equals("anim_time") || variable.equals("time"))
            variable = "query.anim_time";
        return variable;
    }

    public static ExpValue of(String exp) {
        exp = exp.replace(" ", "");
        try {
            // Quick resolve
            double val = Double.parseDouble(exp);
            if (val == 0)
                return ExpValue.DEFAULT;
            return new BuiltinValues.ConstantValue(val);
        } catch (NumberFormatException ignored) {
        }
        Stack<TokenHolder> output = shuntingYard(exp);

        Stack<ExpValue> vars = new Stack<>();
        for (TokenHolder op : output) {
            switch (op.type) {
                case NUMBER -> vars.push(new BuiltinValues.ConstantValue(Double.parseDouble(op.token)));
                case VAR -> {
                    if (op.token.equals("pi") || op.token.equals("PI")) {
                        vars.push(new BuiltinValues.ConstantValue(Math.PI));
                    } else {
                        vars.push(new BuiltinValues.VariableValue(variableEquivalent(op.token)));
                    }
                }
                case SUB_UNARY -> {
                    ExpValue value = vars.pop();
                    vars.push(new BuiltinValues.NegativeValue(value));
                }
                case ADD -> {
                    ExpValue sec = vars.pop();
                    ExpValue first = vars.pop();
                    vars.push(new BuiltinValues.Addition(first, sec));
                }
                case SUB -> {
                    ExpValue sec = vars.pop();
                    ExpValue first = vars.pop();
                    vars.push(new BuiltinValues.Substraction(first, sec));
                }
                case MULT -> {
                    ExpValue sec = vars.pop();
                    ExpValue first = vars.pop();
                    vars.push(new BuiltinValues.Multiplication(first, sec));
                }
                case DIV -> {
                    ExpValue sec = vars.pop();
                    ExpValue first = vars.pop();
                    vars.push(new BuiltinValues.Division(first, sec));
                }
                case FUNC -> {
                    ExpValue value = FunctionRegistry.tryConstruct(op.token, vars);
                    if (value != null)
                        vars.push(value);
                }
            }
        }
        if (vars.size() > 1)
            throw new IllegalStateException("Not all tokens processed! Unable to parse expression " + exp);
        return vars.pop();
    }

    /**
     * Shunting yard algorithm
     */
    private static Stack<TokenHolder> shuntingYard(String exp) {
        Stack<TokenHolder> output = new Stack<>();
        Stack<TokenHolder> operators = new Stack<>();

        Matcher matcher = PATTERN.matcher(exp);
        Type lastType = null;
        while (matcher.find()) {
            String token = stripPrefix(matcher.group());
            Type type = Type.type(token, lastType);
            if (lastType == Type.VAR && type == Type.BRACKETOPEN) {
                throw new IllegalStateException("Unknown function '" + output.getLast().token + "'");
            }
            switch (type) {
                case NUMBER, VAR -> output.add(new TokenHolder(type, token));
                case FUNC, BRACKETOPEN -> operators.add(new TokenHolder(type, token));
                case DELIMITER -> {
                    while (!operators.empty() && operators.peek().type != Type.BRACKETOPEN) {
                        output.add(operators.pop());
                    }
                }
                case BRACKETCLOSE -> {
                    while (!operators.empty() && operators.peek().type != Type.BRACKETOPEN) {
                        output.add(operators.pop());
                    }
                    if (operators.empty()) {
                        throw new IllegalArgumentException("Mismatched brackets!");
                    }
                    operators.pop();
                    if (operators.peek().type == Type.FUNC) {
                        output.add(operators.pop());
                    }
                }
                case MULT, DIV, ADD, SUB, ADD_UNARY, SUB_UNARY -> {
                    TokenHolder op;
                    while (!operators.empty() && (op = operators.peek()).type.isOp()) {
                        if (type.args == 1 && op.type.args == 2) {
                            break;
                        } else if (type.priority <= op.type.priority) {
                            output.add(operators.pop());
                        } else {
                            break;
                        }
                    }
                    operators.add(new TokenHolder(type, token));
                }
            }
            lastType = type;
        }
        while (!operators.empty()) {
            TokenHolder op = operators.pop();
            if (op.type == Type.BRACKETOPEN || op.type == Type.BRACKETCLOSE) {
                throw new IllegalArgumentException("Mismatched brackets!");
            }
            output.push(op);
        }
        return output;
    }

    private static String stripPrefix(String token) {
        if (token.startsWith("math."))
            return token.replaceFirst("math.", "");
        return token;
    }

    record TokenHolder(Type type, String token) {

    }

    enum Type {
        NUMBER(0, 0),
        VAR(0, 0),
        MULT(10, 2),
        DIV(10, 2),
        ADD(5, 2),
        SUB(5, 2),
        SUB_UNARY(100, 1),
        ADD_UNARY(100, 1),
        FUNC(0, -1),
        BRACKETOPEN(999, 0),
        BRACKETCLOSE(999, 0),
        DELIMITER(0, 0);

        public final int priority;
        public final int args;

        Type(int priority, int args) {
            this.priority = priority;
            this.args = args;
        }

        public boolean isOp() {
            return this == MULT || this == DIV || this == ADD || this == SUB ||
                    this == SUB_UNARY || this == ADD_UNARY;
        }

        public static Type type(String s, Type last) {
            if (FunctionRegistry.has(s))
                return Type.FUNC;
            return switch (s) {
                case "," -> Type.DELIMITER;
                case "+" -> {
                    if (last == null || last.args == 2 || last == Type.BRACKETOPEN || last == Type.DELIMITER) {
                        yield Type.ADD_UNARY;
                    }
                    yield Type.ADD;
                }
                case "-" -> {
                    if (last == null || last.args == 2 || last == Type.BRACKETOPEN || last == Type.DELIMITER) {
                        yield Type.SUB_UNARY;
                    }
                    yield Type.SUB;
                }
                case "*" -> Type.MULT;
                case "/" -> Type.DIV;
                case "(" -> Type.BRACKETOPEN;
                case ")" -> Type.BRACKETCLOSE;
                case "pi", "PI" -> Type.VAR;
                default -> {
                    try {
                        Double.parseDouble(s);
                        yield Type.NUMBER;
                    } catch (NumberFormatException ignored) {
                    }
                    yield Type.VAR;
                }
            };
        }
    }

    @TestOnly
    public static void test() {
        ExpValue exp0 = of("2*4+7");
        ExpValue exp1 = of("5+7+3+1*6*3+9");
        ExpValue exp2 = of("5+(44+1)*4*1/6+99");
        ExpValue exp3 = of("-5+5+(-3*5)");

        ExpValue exp4 = of("math.sin(time*(44+1)+3)*4*1/6");
        ExpValue exp5 = of("99*math.cos(1)");
        ExpValue exp6 = of("-math.sin(time*180)+17.5");
        ExpValue exp7 = of("math.sin(time*180)-17.5");

        ExpValue exp8 = of("math.sin(query.anim_time * 1200) * 6 * query.above_top_solid");
        ExpValue exp9 = of("log(5) * sqrt(16) * pow(3,6) * pi");
        ExpValue exp10 = of("rand(3,8)");
        ExpValue exp11 = of("randInt(3,8)");

        System.out.println("Expression 0: " + exp0);
        verify(exp0.get(new VariableMap()), 15);
        System.out.println("Expression 1: " + exp1);
        verify(exp1.get(new VariableMap()), 42);
        System.out.println("Expression 2: " + exp2);
        verify(exp2.get(new VariableMap()), 134);
        System.out.println("Expression 3: " + exp3);
        verify(exp3.get(new VariableMap()), -15);

        System.out.println("Expression 4: " + exp4);
        verify(roundDecimal(exp4.get(new VariableMap().setVariable("query.anim_time", 5)), 5), -0.49543);
        System.out.println("Expression 5: " + exp5);
        verify(roundDecimal(exp5.get(new VariableMap()), 5), 98.98492);
        System.out.println("Expression 6: " + exp6);
        verify(exp6.get(new VariableMap().setVariable("query.anim_time", 0)), 17.5);
        System.out.println("Expression 7: " + exp7);
        verify(exp7.get(new VariableMap().setVariable("query.anim_time", 0)), -17.5);

        System.out.println("Expression 8: " + exp8);
        verify(roundDecimal(exp8.get(new VariableMap().setVariable("query.anim_time", 4)
                .setVariable("query.above_top_solid", 9)), 3), 46.765);

        System.out.println("Expression 9: " + exp9);
        verify(roundDecimal(exp9.get(new VariableMap()), 3), 14743.874);

        System.out.println("Expression 10: " + exp10 + " res " + exp10.get(new VariableMap()));
        System.out.println("Expression 11: " + exp11 + " res " + exp11.get(new VariableMap()));
    }

    private static void verify(double value, double truth) {
        if (value != truth)
            throw new IllegalStateException(String.format("Wrong min. Expected %s but was %s", truth, value));
    }

    private static double roundDecimal(double value, int decimals) {
        double pow = Math.pow(10, decimals);
        return Math.round(value * pow) / pow;
    }
}

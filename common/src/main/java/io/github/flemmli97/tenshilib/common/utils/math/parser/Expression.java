package io.github.flemmli97.tenshilib.common.utils.math.parser;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.BuiltinValues;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.Operators;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.Condition;
import org.jetbrains.annotations.TestOnly;

import java.util.Random;
import java.util.Stack;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Math parser able to create a math expression from a string.
 * Just call {@link Expression#of}
 */
public class Expression {

    private static final String NUMBER = "\\d+(?:\\.\\d+)?";
    private static final String VARIABLE = "(?<!\\.)[A-Za-z][A-Za-z._]*";
    private static final String OPERATOR = Operators.regex() + "|(?:[?:])|[()]";

    private static final Pattern PATTERN = Pattern.compile(String.format("(?:%1$s)|(?:%2$s)|%3$s|,", NUMBER, VARIABLE, OPERATOR));

    public static ExpValue of(String exp) {
        try {
            return ofInternal(exp);
        } catch (Exception e) {
            TenshiLib.LOGGER.error("Could't parse expression {}", exp);
            throw e;
        }
    }

    private static ExpValue ofInternal(String exp) {
        exp = exp.replace(" ", "").replace("\n", "");
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
                        vars.push(new BuiltinValues.VariableValue(op.token));
                    }
                }
                case UNARY_OP -> {
                    switch (op.token) {
                        case "-" -> {
                            ExpValue value = vars.pop();
                            vars.push(new BuiltinValues.NegativeValue(value));
                        }
                        case "+" -> {
                            ExpValue value = vars.pop();
                            vars.push(value);
                        }
                        case "!" -> {
                            ExpValue value = vars.pop();
                            vars.push(new BuiltinValues.NegatedValue(value));
                        }
                    }
                }
                case BINARY_OP -> Operators.get(op.token, vars);
                case TERNARY_END -> {
                    ExpValue otherwise = vars.pop();
                    ExpValue then = vars.pop();
                    ExpValue condition = vars.pop();
                    vars.push(new Condition(condition, then, otherwise));
                }
                case FUNC -> {
                    ExpValue value = FunctionRegistry.tryConstruct(op.token, vars);
                    if (value != null)
                        vars.push(value);
                }
            }
        }
        if (vars.size() > 1)
            throw new IllegalStateException("Not all tokens processed! Unable to parse expression " + exp + " - Leftover Tokens: " + vars);
        return vars.pop();
    }

    /**
     * Shunting yard algorithm
     */
    private static Stack<TokenHolder> shuntingYard(String exp) {
        Stack<TokenHolder> output = new Stack<>();
        Stack<TokenHolder> operators = new Stack<>();

        Matcher matcher = PATTERN.matcher(exp);
        TokenHolder lastType = null;
        while (matcher.find()) {
            String token = stripPrefix(matcher.group());
            TokenHolder holder = TokenHolder.of(token, lastType);
            if (lastType != null && lastType.type() == Type.VAR && holder.type() == Type.BRACKETOPEN) {
                throw new IllegalStateException("Unknown function '" + output.getLast().token + "'");
            }
            switch (holder.type()) {
                case NUMBER, VAR -> output.add(holder);
                case FUNC, BRACKETOPEN -> operators.add(holder);
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
                        throw new IllegalStateException("Mismatched brackets!");
                    }
                    operators.pop();
                    if (!operators.empty() && operators.peek().type == Type.FUNC) {
                        output.add(operators.pop());
                    }
                }
                case UNARY_OP, BINARY_OP, TERNARY_START, TERNARY_END -> {
                    TokenHolder op;
                    while (!operators.empty() && (op = operators.peek()).type.isOp()) {
                        if (holder.precedence() < op.precedence() || (holder.precedence() == op.precedence() && holder.leftAssociative())) {
                            output.add(operators.pop());
                        } else {
                            break;
                        }
                    }
                    operators.add(holder);
                }
            }
            lastType = holder;
        }
        while (!operators.empty()) {
            TokenHolder op = operators.pop();
            if (op.type == Type.BRACKETOPEN || op.type == Type.BRACKETCLOSE) {
                throw new IllegalStateException("Mismatched brackets!");
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

    record TokenHolder(Type type, int precedence, boolean leftAssociative, String token) {

        public TokenHolder(Type type, int precedence, String token) {
            this(type, precedence, true, token);
        }

        public static TokenHolder of(String token, TokenHolder last) {
            if (FunctionRegistry.has(token))
                return new TokenHolder(Type.FUNC, 100, token);
            return switch (token) {
                case "," -> new TokenHolder(Type.DELIMITER, 0, token);
                case "^" -> new TokenHolder(Type.BINARY_OP, 12, false, token);
                case "*", "/", "%" -> new TokenHolder(Type.BINARY_OP, 11, token);
                case "+", "-", "!" -> {
                    if (last == null || last.type().args > 1 || last.type() == Type.BRACKETOPEN || last.type() == Type.DELIMITER) {
                        yield new TokenHolder(Type.UNARY_OP, 100, false, token);
                    }
                    yield new TokenHolder(Type.BINARY_OP, 10, token);
                }
                case "<", "<=", ">", ">=" -> new TokenHolder(Type.BINARY_OP, 9, token);
                case "==", "!=" -> new TokenHolder(Type.BINARY_OP, 8, token);
                case "&&" -> new TokenHolder(Type.BINARY_OP, 7, token);
                case "||" -> new TokenHolder(Type.BINARY_OP, 6, token);
                case "?:" -> new TokenHolder(Type.BINARY_OP, 5, token);
                case "?" -> new TokenHolder(Type.TERNARY_START, 5, token);
                case ":" -> new TokenHolder(Type.TERNARY_END, 4, token);

                case "(" -> new TokenHolder(Type.BRACKETOPEN, 0, token);
                case ")" -> new TokenHolder(Type.BRACKETCLOSE, 0, token);
                case "pi", "PI" -> new TokenHolder(Type.VAR, 0, token);
                default -> {
                    try {
                        Double.parseDouble(token);
                        yield new TokenHolder(Type.NUMBER, 0, token);
                    } catch (NumberFormatException ignored) {
                    }
                    yield new TokenHolder(Type.VAR, 0, token);
                }
            };
        }
    }

    public enum Type {
        NUMBER(0),
        VAR(0),
        UNARY_OP(1),
        BINARY_OP(2),
        TERNARY_START(3),
        TERNARY_END(3),
        FUNC(-1),
        BRACKETOPEN(0),
        BRACKETCLOSE(0),
        DELIMITER(0);

        public final int args;

        Type(int args) {
            this.args = args;
        }

        public boolean isOp() {
            return this == UNARY_OP || this == BINARY_OP || this == TERNARY_START || this == TERNARY_END;
        }
    }

    @TestOnly
    public static void test() {
        verify("1+2+3+4", new VariableMap(), 0, 10);
        verify("-1+2+-3+4", new VariableMap(), 0, 2);
        verify("2*4+7", new VariableMap(), 0, 15);
        verify("2*-4+7+3", new VariableMap(), 0, 2);
        verify("5+7+3+1*6*3+9", new VariableMap(), 0, 42);
        verify("5+(44+1)*4*1/6+99", new VariableMap(), 0, 134);
        verify("-5+5+(-3*5)", new VariableMap(), 0, -15);
        verify("10 * (x + 5)", new VariableMap().setVariable("x", 2), 0, 70);
        verify("(x + 5) * 10", new VariableMap().setVariable("x", 2), 0, 70);

        verify("math.sin(time*(44+1)+3)*4*1/6", new VariableMap().setVariable("query.anim_time", 5), 5, -0.49543);
        verify("99*math.cos(1)", new VariableMap(), 5, 98.98492);
        verify("-math.sin(time*180)+17.5", new VariableMap().setVariable("query.anim_time", 0), 0, 17.5);
        verify("1 + math.sin(time*180)-17.5", new VariableMap().setVariable("query.anim_time", 0), 0, -16.5);
        verify("math.sin(query.anim_time * 1200) * 6 * query.above_top_solid", new VariableMap().setVariable("query.anim_time", 4)
                .setVariable("query.above_top_solid", 9), 3, 46.765);
        verify("ln(5) * sqrt(16) * 3^6 * pi", new VariableMap(), 3, 14743.874);
        verify("5 % 3", new VariableMap(), 0, 2);
        verify("5 % 7", new VariableMap(), 0, 5);
        verify("2^2^2", new VariableMap(), 0, 16);
        ExpValue exp10 = of("random(3,8)");
        System.out.println("Expression 10: " + exp10 + " res " + exp10.get(new VariableMap()));
        ExpValue exp11 = of("random_integer(3,8)");
        System.out.println("Expression 11: " + exp11 + " res " + exp11.get(new VariableMap()));

        verify("5 == 5", new VariableMap(), 0, 1);
        verify("5 == 6", new VariableMap(), 0, 0);
        verify("5 != 6", new VariableMap(), 0, 1);
        verifyBool("<", new VariableMap(), 1000, 20, (f, s) -> f < s);
        verifyBool("<=", new VariableMap(), 10, 40, (f, s) -> f <= s);
        verifyBool(">", new VariableMap(), 1000, 20, (f, s) -> f > s);
        verifyBool(">=", new VariableMap(), 10, 40, (f, s) -> f >= s);
        verify("5 == 6 || 5 == 5", new VariableMap(), 0, 1);
        verify("5 == 6 || 5 == 7", new VariableMap(), 0, 0);
        verify("5 == 6 && 5 == 5", new VariableMap(), 0, 0);
        verify("5 == 5 && 3 <= 3", new VariableMap(), 0, 1);
        verify("5 == 5 ? 6 + 3 : 3 + 1", new VariableMap(), 0, 9);
        verify("5 != 5 ? 6 + 3 : 3 ^ 3", new VariableMap(), 0, 27);
    }

    private static void verifyBool(String op, VariableMap vars, int bounds, int amount, BiPredicate<Integer, Integer> check) {
        for (int i = 0; i < amount; i++) {
            int first = new Random().nextInt(bounds);
            int second = new Random().nextInt(bounds);
            System.out.println("Evaluating expression: " + first + op + second);
            ExpValue expression = Expression.of(first + op + second);
            System.out.println("Parsed: " + expression);
            verify(expression.get(vars), check.test(first, second) ? 1 : 0);
        }
    }

    private static void verify(String input, VariableMap vars, int decimals, double truth) {
        System.out.println("Evaluating expression: " + input);
        ExpValue expression = Expression.of(input);
        System.out.println("Parsed: " + expression);
        if (decimals == 0)
            verify(expression.get(vars), truth);
        else
            verify(roundDecimal(expression.get(vars), decimals), truth);
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

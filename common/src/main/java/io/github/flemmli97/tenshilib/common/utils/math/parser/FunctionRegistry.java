package io.github.flemmli97.tenshilib.common.utils.math.parser;

import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.ACos;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.ASin;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.ATan;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.ATan2;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Abs;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Ceil;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Clamp;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Cos;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Exp;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Floor;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Log;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Max;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Min;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Random;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.RandomInt;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Round;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Sin;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Sqrt;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.functions.Tan;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class FunctionRegistry {

    private static final Map<String, Function<Stack<ExpValue>, ExpValue>> PARSER = init();

    private static Map<String, Function<Stack<ExpValue>, ExpValue>> init() {
        Map<String, Function<Stack<ExpValue>, ExpValue>> builtin = new HashMap<>();
        builtin.put("sin", stack -> {
            ExpValue value = stack.pop();
            return new Sin(value);
        });
        builtin.put("asin", stack -> {
            ExpValue value = stack.pop();
            return new ASin(value);
        });
        builtin.put("cos", stack -> {
            ExpValue value = stack.pop();
            return new Cos(value);
        });
        builtin.put("acos", stack -> {
            ExpValue value = stack.pop();
            return new ACos(value);
        });
        builtin.put("tan", stack -> {
            ExpValue value = stack.pop();
            return new Tan(value);
        });
        builtin.put("atan", stack -> {
            ExpValue value = stack.pop();
            return new ATan(value);
        });
        builtin.put("atan2", stack -> {
            ExpValue b = stack.pop();
            ExpValue a = stack.pop();
            return new ATan2(a, b);
        });
        builtin.put("abs", stack -> {
            ExpValue value = stack.pop();
            return new Abs(value);
        });
        builtin.put("min", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new Min(first, second);
        });
        builtin.put("max", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new Max(first, second);
        });
        builtin.put("clamp", stack -> {
            ExpValue max = stack.pop();
            ExpValue min = stack.pop();
            ExpValue val = stack.pop();
            return new Clamp(val, min, max);
        });
        builtin.put("sqrt", stack -> {
            ExpValue value = stack.pop();
            return new Sqrt(value);
        });
        builtin.put("ln", stack -> {
            ExpValue value = stack.pop();
            return new Log(value);
        });
        builtin.put("exp", stack -> {
            ExpValue value = stack.pop();
            return new Exp(value);
        });
        builtin.put("floor", stack -> {
            ExpValue value = stack.pop();
            return new Floor(value);
        });
        builtin.put("ceil", stack -> {
            ExpValue value = stack.pop();
            return new Ceil(value);
        });
        builtin.put("round", stack -> {
            ExpValue value = stack.pop();
            return new Round(value);
        });
        builtin.put("random", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new Random(first, second);
        });
        builtin.put("random_integer", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new RandomInt(first, second);
        });
        return builtin;
    }

    /**
     * Registers a new function
     * Use the provided stack to get access to the functions arguments
     * Note that the stack is in reversed order so if you have a multi arg function popping the value returns the LAST arg
     */
    public static synchronized void register(String identifier, Function<Stack<ExpValue>, ExpValue> func) {
        if (PARSER.put(identifier, func) != null) {
            throw new IllegalStateException("Function already registered: " + identifier);
        }
    }

    public static boolean has(String identifier) {
        return PARSER.get(identifier.replace("math.", "")) != null;
    }

    public static ExpValue tryConstruct(String identifier, Stack<ExpValue> stack) {
        Function<Stack<ExpValue>, ExpValue> func = PARSER.get(identifier.replace("math.", ""));
        if (func == null)
            return null;
        try {
            return func.apply(stack);
        } catch (EmptyStackException e) {
            System.out.println("Mismatched function args");
        }
        return null;
    }
}

package io.github.flemmli97.tenshilib.common.utils.math.parser;

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
            return new BuiltinFunctions.Sin(value);
        });
        builtin.put("cos", stack -> {
            ExpValue value = stack.pop();
            return new BuiltinFunctions.Cos(value);
        });
        builtin.put("abs", stack -> {
            ExpValue value = stack.pop();
            return new BuiltinFunctions.Abs(value);
        });
        builtin.put("min", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new BuiltinFunctions.Min(first, second);
        });
        builtin.put("max", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new BuiltinFunctions.Max(first, second);
        });
        builtin.put("sqrt", stack -> {
            ExpValue value = stack.pop();
            return new BuiltinFunctions.Sqrt(value);
        });
        builtin.put("log", stack -> {
            ExpValue value = stack.pop();
            return new BuiltinFunctions.Log(value);
        });
        builtin.put("pow", stack -> {
            ExpValue exponent = stack.pop();
            ExpValue base = stack.pop();
            return new BuiltinFunctions.Power(base, exponent);
        });
        builtin.put("rand", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new BuiltinFunctions.Random(first, second);
        });
        builtin.put("randInt", stack -> {
            ExpValue second = stack.pop();
            ExpValue first = stack.pop();
            return new BuiltinFunctions.RandomInt(first, second);
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

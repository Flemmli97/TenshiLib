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
        return builtin;
    }

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

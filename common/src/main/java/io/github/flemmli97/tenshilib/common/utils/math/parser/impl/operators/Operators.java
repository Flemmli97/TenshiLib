package io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.And;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.Condition;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.Equals;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.Greater;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.GreaterThan;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.Less;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.LessThan;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.NotEquals;
import io.github.flemmli97.tenshilib.common.utils.math.parser.impl.operators.logic.Or;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Operators {

    private static final Map<String, Consumer<Stack<ExpValue>>> OPERATORS = init();

    private static Map<String, Consumer<Stack<ExpValue>>> init() {
        Map<String, Consumer<Stack<ExpValue>>> builtin = new HashMap<>();
        builtin.put("+", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Addition(first, sec));
        });
        builtin.put("-", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Substraction(first, sec));
        });
        builtin.put("*", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Multiplication(first, sec));
        });
        builtin.put("/", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Division(first, sec));
        });
        builtin.put("^", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Power(first, sec));
        });
        builtin.put("%", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Modulo(first, sec));
        });
        // Logical ops
        builtin.put("==", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Equals(first, sec));
        });
        builtin.put("!=", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new NotEquals(first, sec));
        });
        builtin.put("<", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Less(first, sec));
        });
        builtin.put("<=", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new LessThan(first, sec));
        });
        builtin.put(">", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Greater(first, sec));
        });
        builtin.put(">=", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new GreaterThan(first, sec));
        });
        builtin.put("&&", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new And(first, sec));
        });
        builtin.put("||", vars -> {
            ExpValue sec = vars.pop();
            ExpValue first = vars.pop();
            vars.push(new Or(first, sec));
        });
        builtin.put("?:", vars -> {
            ExpValue then = vars.pop();
            ExpValue condition = vars.pop();
            vars.push(new Condition(condition, then));
        });
        return builtin;
    }

    public static String regex() {
        List<String> patterns = new ArrayList<>();
        // Should try to match longer ops first as e.g. < will override <= otherwise
        List<String> sort = OPERATORS.keySet().stream().sorted((f, s) -> Integer.compare(s.length(), f.length())).toList();
        for (String op : sort) {
            patterns.add("(?:" + Pattern.quote(op) + ")");
        }
        return String.join("|", patterns);
    }

    public static void get(String identifier, Stack<ExpValue> stack) {
        Consumer<Stack<ExpValue>> consumer = OPERATORS.get(identifier);
        if (consumer == null)
            return;
        try {
            consumer.accept(stack);
        } catch (EmptyStackException e) {
            System.out.println("Mismatched operator args");
        }
    }
}

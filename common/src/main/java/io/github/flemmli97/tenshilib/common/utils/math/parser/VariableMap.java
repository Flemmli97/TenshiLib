package io.github.flemmli97.tenshilib.common.utils.math.parser;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class VariableMap {

    private final Object2DoubleMap<String> variables = new Object2DoubleOpenHashMap<>();
    private final Map<String, DoubleSupplier> lazyVariables = new HashMap<>();

    private RandomSource random = RandomSource.createNewThreadLocalInstance();

    public VariableMap setVariable(String variable, double value) {
        this.variables.put(variable, value);
        return this;
    }

    /**
     * Set a lazy evaluated variable
     */
    public VariableMap setVariable(String variable, DoubleSupplier value) {
        this.lazyVariables.put(variable, value);
        return this;
    }

    public VariableMap clear() {
        this.variables.clear();
        this.lazyVariables.clear();
        return this;
    }

    public VariableMap withRandom(RandomSource random) {
        this.random = random;
        return this;
    }

    public double getValue(String variable) {
        DoubleSupplier sup = this.lazyVariables.get(variable);
        if (sup != null)
            return sup.getAsDouble();
        return this.variables.getOrDefault(variable, 0);
    }

    public RandomSource random() {
        return this.random;
    }
}

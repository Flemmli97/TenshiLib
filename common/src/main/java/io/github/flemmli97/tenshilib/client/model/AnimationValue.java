package io.github.flemmli97.tenshilib.client.model;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public class AnimationValue {

    public final float startTick;
    private final ExpValue xVal, yVal, zVal;

    public AnimationValue(float startTick, ExpValue xVal, ExpValue yVal, ExpValue zVal) {
        this.startTick = startTick;
        this.xVal = xVal;
        this.yVal = yVal;
        this.zVal = zVal;
    }

    public float getXVal(VariableMap vars) {
        return this.xVal.asFloat(vars);
    }

    public float getYVal(VariableMap vars) {
        return this.yVal.asFloat(vars);
    }

    public float getZVal(VariableMap vars) {
        return this.zVal.asFloat(vars);
    }

    @Override
    public String toString() {
        return String.format("[S:%s, {%s,%s,%s}", this.startTick, this.xVal, this.yVal, this.zVal);
    }
}

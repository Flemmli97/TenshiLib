package io.github.flemmli97.tenshilib.client.model.animation.keyframe;

import io.github.flemmli97.tenshilib.common.utils.math.parser.ExpValue;
import io.github.flemmli97.tenshilib.common.utils.math.parser.Expression;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;

public class BoneKeyFrame extends KeyFrameValue {

    private final ExpValue xVal, yVal, zVal;

    public BoneKeyFrame(double startTick, String xVal, String yVal, String zVal) {
        super(startTick);
        this.xVal = Expression.of(xVal);
        this.yVal = Expression.of(yVal);
        this.zVal = Expression.of(zVal);
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
        return String.format("Bone Keyframe [@:%s, {%s, %s, %s}]", this.startTick, this.xVal, this.yVal, this.zVal);
    }
}

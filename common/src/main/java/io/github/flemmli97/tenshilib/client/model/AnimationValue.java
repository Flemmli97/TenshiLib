package io.github.flemmli97.tenshilib.client.model;

public class AnimationValue {

    public final float startTick;
    private final SimpleAnimationExpression.Value xVal, yVal, zVal;

    public AnimationValue(float startTick, SimpleAnimationExpression.Value xVal, SimpleAnimationExpression.Value yVal, SimpleAnimationExpression.Value zVal) {
        this.startTick = startTick;
        this.xVal = xVal;
        this.yVal = yVal;
        this.zVal = zVal;
    }

    public float getXVal(SimpleAnimationExpression.VariableMap vars) {
        return this.xVal.get(vars);
    }

    public float getYVal(SimpleAnimationExpression.VariableMap vars) {
        return this.yVal.get(vars);
    }

    public float getZVal(SimpleAnimationExpression.VariableMap vars) {
        return this.zVal.get(vars);
    }

    @Override
    public String toString() {
        return String.format("[S:%s, {%s,%s,%s}", this.startTick, this.xVal, this.yVal, this.zVal);
    }
}

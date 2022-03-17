package io.github.flemmli97.tenshilib.client.model;

public class AnimationValue {

    public final int startTick;
    private final SimpleAnimationExpression.Value xVal, yVal, zVal;

    public AnimationValue(int startTick, SimpleAnimationExpression.Value xVal, SimpleAnimationExpression.Value yVal, SimpleAnimationExpression.Value zVal) {
        this.startTick = startTick;
        this.xVal = xVal;
        this.yVal = yVal;
        this.zVal = zVal;
    }

    public float getXVal(float time) {
        return this.xVal.get(time);
    }

    public float getYVal(float time) {
        return this.yVal.get(time);
    }

    public float getZVal(float time) {
        return this.zVal.get(time);
    }

    @Override
    public String toString() {
        return String.format("[S:%s, {%s,%s,%s}", this.startTick, this.xVal, this.yVal, this.zVal);
    }
}

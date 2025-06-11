package io.github.flemmli97.tenshilib.client.model.animation.keyframe;

import org.jetbrains.annotations.NotNull;

public class KeyFrameValue implements Comparable<KeyFrameValue> {

    public final double startTick;

    public KeyFrameValue(double startTick) {
        this.startTick = startTick;
    }

    @Override
    public int compareTo(@NotNull KeyFrameValue keyFrameValue) {
        return Double.compare(this.startTick, keyFrameValue.startTick);
    }
}

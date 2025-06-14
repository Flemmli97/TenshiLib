package io.github.flemmli97.tenshilib.common.entity.animated;

import net.minecraft.util.Mth;

/**
 * Active animation running currently on an entity
 */
public class AnimationState {

    private final AnimationDefinition data;
    private final double speed;

    private final int startTransition, endTransition;

    private double offset;
    private double ticker;

    private AnimationState(AnimationDefinition data, double speed, int startTransition, int endTransition) {
        this.data = data;
        this.speed = speed;
        this.startTransition = startTransition;
        this.endTransition = endTransition;
    }

    public static AnimationState create(AnimationDefinition data) {
        return create(data, 1);
    }

    public static AnimationState create(AnimationDefinition data, double speed) {
        return create(data, AnimationHandler.FALLBACK_TRANSIT_TIME, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, speed);
    }

    /**
     * Creates a new copy instance of the animation with the given modifiers
     */
    public static AnimationState create(AnimationDefinition data, int startTransition, int endTransition, double offset, double speed) {
        AnimationState state = new AnimationState(data,
                data.speed() * speed,
                data.startTransition() > 0 && startTransition == AnimationHandler.FALLBACK_TRANSIT_TIME ? data.startTransition() : startTransition,
                data.endTransition() > 0 && endTransition == AnimationHandler.FALLBACK_TRANSIT_TIME ? data.endTransition() : endTransition);
        state.ticker = offset;
        state.offset = offset;
        return state;
    }

    public AnimationDefinition definition() {
        return this.data;
    }

    public boolean tick() {
        return this.tick(1);
    }

    public boolean tick(int offset) {
        return (this.ticker += this.speed) >= (this.data.length() + this.startTransition + offset) && this.data.shouldRunOut();
    }

    public boolean done(int offset) {
        return this.ticker >= (this.data.length() + this.startTransition + offset);
    }

    public double getSpeed() {
        return this.speed;
    }

    /**
     * How far the animation has progressed towards the end
     */
    public double progress(float partialTicks) {
        return this.progress(0, this.data.length(), partialTicks, 0);
    }

    /**
     * Get the progress in between the given value
     *
     * @param start Start value in ticks
     * @param end   End value in ticks
     */
    public double progress(double start, double end, float partialTicks, int offset) {
        double tick = this.getTick(partialTicks) + offset * this.speed;
        double length = end - start;
        return Mth.clamp((tick - start) / length, 0, 1);
    }

    public double getStartTransitionProgress(float partialTicks) {
        if (this.startTransition == 0) {
            return 1;
        }
        double tick = Math.max(0, this.ticker - this.offset - 1 + partialTicks * this.speed);
        return Mth.clamp(tick / this.getStartTransition(), 0, 1);
    }

    public int getStartTransition() {
        return this.startTransition;
    }

    public int getEndTransitionTime() {
        return this.endTransition;
    }

    public float getTick(float partialTicks) {
        return (float) Math.max(this.offset, this.ticker - this.startTransition - 1 + partialTicks * this.speed);
    }

    public boolean isAt(double time) {
        return this.isAt(time, true);
    }

    /**
     * @param seconds Whether the given time is in seconds or ticks
     * @return True if the current animation is at the given tick
     */
    public boolean isAt(double time, boolean seconds) {
        double tick = (seconds ? time * 20 : time);
        double current = this.getTick(1);
        double last = current - this.speed;
        return last < tick && current >= tick;
    }

    public boolean isPast(double time) {
        return this.isPast(time, true);
    }

    /**
     * @param seconds Whether the given time is in seconds or ticks
     * @return True if the current animation is past the given tick
     */
    public boolean isPast(double time, boolean seconds) {
        double tick = (seconds ? time * 20 : time);
        return this.getTick(1) >= tick;
    }

    public boolean isBetween(double start, double end) {
        return this.isBetween(start, end, true);
    }

    /**
     * @param seconds Whether the given time is in seconds or ticks
     * @return True if the current animation is between the given range
     */
    public boolean isBetween(double start, double end, boolean seconds) {
        double startTick = (seconds ? start * 20 : start);
        double endTick = (seconds ? end * 20 : end);
        double tick = this.getTick(1);
        return tick >= startTick && tick <= endTick;
    }

    public boolean isAt(String marker) {
        double[] times = this.data.markers().get(marker);
        if (times == null) {
            return false;
        }
        for (double time : times) {
            if (this.isAt(time))
                return true;
        }
        return false;
    }

    public boolean isPast(String marker) {
        return this.isPast(marker, -1);
    }

    /**
     * @param max The upper time in seconds
     */
    public boolean isPast(String marker, double max) {
        double[] times = this.data.markers().get(marker);
        if (times == null) {
            return false;
        }
        for (double time : times) {
            boolean matches = max >= 0 ? this.isBetween(time, max) : this.isPast(time);
            if (matches)
                return true;
        }
        return false;
    }

    public double getLength() {
        return this.data.length();
    }

    public void reset() {
        this.ticker = 0;
    }

    public String getID() {
        return this.data.id();
    }

    /**
     * Get the marker value of the given index
     *
     * @return -1 If the marker is not present
     */
    public double getMarker(String marker, int index) {
        double[] times = this.data.markers().get(marker);
        if (times == null || index >= times.length)
            return -1;
        return times[index];
    }

    public boolean is(AnimationState... others) {
        for (AnimationState other : others)
            if (other != null && this.getID().equals(other.getID()))
                return true;
        return false;
    }

    public boolean is(AnimationDefinition... definitions) {
        return this.data.is(definitions);
    }

    public boolean is(String... ids) {
        return this.data.is(ids);
    }

    public String getAnimation() {
        return this.data.animation();
    }

    /**
     * If false animation will only change if set manually. Else if it ticks out it gets set to null
     */
    public boolean shouldRunOut() {
        return this.data.shouldRunOut();
    }

    @Override
    public String toString() {
        return String.format("ID: %s, length: %s, speed: %s", this.getID(), this.getLength(), this.speed);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AnimationState)
            return this.toString().equals(o.toString());
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}

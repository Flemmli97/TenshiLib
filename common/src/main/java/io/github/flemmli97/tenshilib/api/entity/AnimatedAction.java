package io.github.flemmli97.tenshilib.api.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Mth;

import java.util.Map;

public class AnimatedAction {

    private final float length;
    private final boolean shouldRunOut;

    private final String id, clientIdentifier;

    private final float speed;

    private final int startTransition, endTransition;

    private final Map<String, double[]> markerMap;

    private float offset;
    private float ticker;

    /**
     * @param length Length of the animation in seconds
     * @param id     Unique id for the animation
     */
    public AnimatedAction(double length, String id) {
        this(length, id, true);
    }

    /**
     * @param length  Length of the animation
     * @param id      Unique id for the animation
     * @param seconds If the length is in seconds or ticks
     */
    public AnimatedAction(double length, String id, boolean seconds) {
        this((float) (seconds ? length * 20 : length), id, id, 0, AnimationHandler.DEFAULT_TRANSIT_TIME, 1, true, Map.of());
    }

    /**
     * Use the builder {@link #builder}
     */
    private AnimatedAction(float length, String id, String clientIdentifier, int startTransition, int endTransition, float speedMod, boolean shouldRunOut,
                           Map<String, double[]> markerMap) {
        this.length = Math.max(1, length);
        this.speed = speedMod;
        this.id = id;
        this.clientIdentifier = clientIdentifier;
        this.shouldRunOut = shouldRunOut;
        this.startTransition = Math.max(0, startTransition);
        this.endTransition = Math.max(0, endTransition);
        this.markerMap = markerMap;
    }

    public static AnimatedAction copyOf(AnimatedAction animatedAction, String id) {
        return new AnimatedAction(animatedAction.length, id, animatedAction.clientIdentifier,
                animatedAction.startTransition, animatedAction.endTransition, animatedAction.speed, animatedAction.shouldRunOut,
                animatedAction.markerMap);
    }

    public static AnimatedAction.Builder builder(double length, String id) {
        return builder(length, id, true);
    }

    public static AnimatedAction.Builder builder(double length, String id, boolean seconds) {
        return new Builder((float) (seconds ? length * 20 : length), id);
    }

    /**
     * @return Creates a new copy instance of the animation
     */
    public AnimatedAction create() {
        return this.create(1);
    }

    public AnimatedAction create(float speed) {
        return this.create(AnimationHandler.FALLBACK_TRANSIT_TIME, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, speed);
    }

    /**
     * Creates a new copy instance of the animation with the given modifiers
     */
    public AnimatedAction create(int startTransition, int endTransition, float offset, float speed) {
        AnimatedAction anim = new AnimatedAction(this.length, this.id, this.clientIdentifier,
                this.startTransition > 0 && startTransition == AnimationHandler.FALLBACK_TRANSIT_TIME ? this.startTransition : startTransition,
                this.endTransition > 0 && endTransition == AnimationHandler.FALLBACK_TRANSIT_TIME ? this.endTransition : endTransition, this.speed * speed, this.shouldRunOut,
                this.markerMap);
        anim.ticker = offset;
        anim.offset = offset;
        return anim;
    }

    public boolean tick() {
        return this.tick(1);
    }

    public boolean tick(int offset) {
        return (this.ticker += this.speed) >= (this.length + this.startTransition + offset) && this.shouldRunOut;
    }

    public boolean done(int offset) {
        return this.ticker >= (this.length + this.startTransition + offset);
    }

    public float getSpeed() {
        return this.speed;
    }

    /**
     * How far the animation has progressed towards the end
     */
    public float progress(float partialTicks) {
        return this.progress(0, this.length, partialTicks, 0);
    }

    /**
     * Get the progress in between the given value
     *
     * @param start Start value in ticks
     * @param end   End value in ticks
     */
    public float progress(float start, float end, float partialTicks, int offset) {
        float tick = this.getTick(partialTicks) + offset * this.speed;
        float length = end - start;
        return Mth.clamp((tick - start) / length, 0, 1);
    }

    public float getStartTransitionProgress(float partialTicks) {
        if (this.startTransition == 0) {
            return 1;
        }
        float tick = Math.max(0, this.ticker - this.offset - 1 + partialTicks * this.speed);
        return Mth.clamp(tick / this.getStartTransition(), 0, 1);
    }

    public int getStartTransition() {
        return this.startTransition;
    }

    public int getEndTransitionTime() {
        return this.endTransition;
    }

    public float getTick(float partialTicks) {
        return Math.max(this.offset, this.ticker - this.startTransition - 1 + partialTicks * this.speed);
    }

    public boolean isAt(double time) {
        return this.isAt(time, true);
    }

    /**
     * @param seconds Whether the given time is in seconds or ticks
     * @return True if the current animation is at the given tick
     */
    public boolean isAt(double time, boolean seconds) {
        float tick = (float) (seconds ? time * 20 : time);
        float current = this.getTick(1);
        float last = current - this.speed;
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
        float tick = (float) (seconds ? time * 20 : time);
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
        float startTick = (float) (seconds ? start * 20 : start);
        float endTick = (float) (seconds ? end * 20 : end);
        float tick = this.getTick(1);
        return tick >= startTick && tick <= endTick;
    }

    public boolean isAt(String marker) {
        double[] times = this.markerMap.get(marker);
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
        double[] times = this.markerMap.get(marker);
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

    public float getLength() {
        return this.length;
    }

    public void reset() {
        this.ticker = 0;
    }

    public String getID() {
        return this.id;
    }

    /**
     * Get the marker value of the given index
     *
     * @return -1 If the marker is not present
     */
    public double getMarker(String marker, int index) {
        double[] times = this.markerMap.get(marker);
        if (times == null || index >= times.length)
            return -1;
        return times[index];
    }

    public boolean is(AnimatedAction... others) {
        for (AnimatedAction other : others)
            if (other != null && this.id.equals(other.id))
                return true;
        return false;
    }

    public String getClientIdentifier() {
        return this.clientIdentifier;
    }

    /**
     * If false animation will only change if set manually. Else if it ticks out it gets set to null
     */
    public boolean shouldRunOut() {
        return this.shouldRunOut;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, length: %s, speed: %s", this.id, this.length, this.speed);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AnimatedAction)
            return this.toString().equals(o.toString());
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public static class Builder {

        private final float length;
        private boolean shouldRunOut = true;

        private final String id;
        private String clientIdentifier;

        private float speed = 1;

        private int startTransition, endTransition = AnimationHandler.DEFAULT_TRANSIT_TIME;

        private final ImmutableMap.Builder<String, double[]> markerMap = new ImmutableMap.Builder<>();

        /**
         * @param length Length of the animation
         * @param id     Unique id for the animation
         */
        private Builder(float length, String id) {
            this.length = Math.max(1, length);
            this.id = id;
            this.clientIdentifier = id;
        }

        /**
         * The animation to play on the client side. Normally same as id but for cases
         * where you have multiple attacks with same animation set this
         */
        public Builder withClientID(String id) {
            this.clientIdentifier = id;
            return this;
        }

        /**
         * A marker for various things e.g. when the entity should actually do damage
         * Example in a sword slash do the damage mid swing and not at the beginning.
         * Time in seconds not ticks
         */
        public Builder marker(String identifier, double... time) {
            this.markerMap.put(identifier, time);
            return this;
        }

        /**
         * If set the animation will not be set to null once it passes its length. You would need to manually set to null then.
         * Useful for infinite animation (till a condition)
         */
        public Builder infinite() {
            this.shouldRunOut = false;
            return this;
        }

        /**
         * A modifier in the animations speed. Do note that animation ticks are still in integers
         */
        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Time in ticks to transition to/from this animation
         */
        public Builder withTransitionTime(int start, int end) {
            this.startTransition = start;
            this.endTransition = end;
            return this;
        }

        public AnimatedAction build() {
            return new AnimatedAction(this.length, this.id, this.clientIdentifier,
                    this.startTransition, this.endTransition, this.speed, this.shouldRunOut, this.markerMap.build());
        }
    }
}

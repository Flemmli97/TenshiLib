package io.github.flemmli97.tenshilib.api.entity;

import net.minecraft.util.Mth;

public class AnimatedAction {

    private final int length, attackTime;
    private final boolean shouldRunOut;

    private final String id, clientIdentifier;

    private final float speed;

    private final int startTransition, endTransition;

    private float ticker;

    /**
     * @param length Length of the animation in seconds
     * @param id     Unique id for the animation
     */
    public AnimatedAction(double length, String id) {
        this(Mth.ceil(length * 20), id);
    }

    /**
     * @param length Length of the animation in ticks
     * @param id     Unique id for the animation
     */
    public AnimatedAction(int length, String id) {
        this(length, 1, id, id, 0, AnimationHandler.DEFAULT_TRANSIT_TIME, 1, true);
    }

    /**
     * @param length     Length of the animation in seconds
     * @param id         Unique id for the animation
     * @param attackTime A flag for various things e.g. when the entity should actually do damage
     *                   Example in a sword slash do the damage mid swing and not at the beginning.
     */
    public AnimatedAction(double length, double attackTime, String id) {
        this(Mth.ceil(length * 20), Mth.ceil(attackTime * 20), id, id, 0, AnimationHandler.DEFAULT_TRANSIT_TIME, 1, true);
    }

    /**
     * Use the builder {@link #builder}
     */
    private AnimatedAction(int length, int attackTime, String id, String clientIdentifier, int startTransition, int endTransition, float speedMod, boolean shouldRunOut) {
        this.length = Math.max(1, length);
        this.speed = speedMod;
        this.id = id;
        this.attackTime = Mth.clamp(attackTime, 1, this.length);
        this.clientIdentifier = clientIdentifier;
        this.shouldRunOut = shouldRunOut;
        this.startTransition = Math.max(0, startTransition);
        this.endTransition = Math.max(0, endTransition);
    }

    public static AnimatedAction copyOf(AnimatedAction animatedAction, String id) {
        return new AnimatedAction(animatedAction.length, animatedAction.attackTime, id, animatedAction.clientIdentifier,
                animatedAction.startTransition, animatedAction.endTransition, animatedAction.speed, animatedAction.shouldRunOut);
    }

    public static AnimatedAction.Builder builder(float length, String id) {
        return builder(Mth.ceil(length * 20), id);
    }

    public static AnimatedAction.Builder builder(int length, String id) {
        return new Builder(length, id);
    }

    /**
     * @return Creates a new copy instance of the animation
     */
    public AnimatedAction create() {
        return this.create(this.speed);
    }

    /**
     * @return Creates a new copy instance of the animation with the given speed modifier
     */
    public AnimatedAction create(float speed) {
        return this.create(-1, -1, 0, speed);
    }

    public AnimatedAction create(int startTransition, int endTransition, float offset, float speed) {
        AnimatedAction anim = new AnimatedAction(this.length, this.attackTime, this.id, this.clientIdentifier,
                this.startTransition > 0 && startTransition == -1 ? this.startTransition : startTransition,
                this.endTransition > 0 && endTransition == -1 ? this.endTransition : endTransition, speed, this.shouldRunOut);
        anim.ticker = offset;
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

    public boolean canAttack() {
        return this.isAtTick(this.attackTime);
    }

    public float progress(float partialTicks) {
        return Mth.clamp(this.adjustedTick() - 1 + partialTicks / this.length, 0, 1);
    }

    public float getStartTransitionProgress(float partialTicks) {
        if (this.startTransition == 0) {
            return 1;
        }
        return Mth.clamp(this.ticker - 1 + partialTicks / this.startTransition, 0, 1);
    }

    public int getStartTransition() {
        return this.startTransition;
    }

    public int getEndTransitionTime() {
        return this.endTransition;
    }

    private float adjustedTick() {
        return this.ticker - this.startTransition;
    }

    public float getTick(float partialTicks) {
        return Math.max(this.adjustedTick() - 1 + partialTicks, 0);
    }

    public boolean isAtTick(double tick) {
        return this.isAtTick(Mth.ceil(tick * 20));
    }

    /**
     * @return True if the current animation is at the given tick. Use this instead of #getTick() == tick since this respects animation speed
     */
    public boolean isAtTick(int tick) {
        float current = this.adjustedTick();
        if (this.speed == 1)
            return current == tick;
        if (this.speed < 1) {
            int last = (int) (current - this.speed);
            int currentInt = (int) current;
            return last != tick && currentInt == tick;
        }
        float next = current + this.speed;
        return current <= tick && tick < next;
    }

    public boolean isPastTick(double tick) {
        return this.isPastTick((int) Math.ceil(tick * 20));
    }

    /**
     * @return True if the current animation is past the given tick
     */
    public boolean isPastTick(int tick) {
        return this.adjustedTick() >= tick;
    }

    public int getLength() {
        return this.length;
    }

    public int getAttackTime() {
        return this.attackTime;
    }

    public void reset() {
        this.ticker = 0;
    }

    public String getID() {
        return this.id;
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

        private final int length;
        private int attackTime = 1;
        private boolean shouldRunOut = true;

        private final String id;
        private String clientIdentifier;

        private float speed = 1;

        private int startTransition, endTransition = AnimationHandler.DEFAULT_TRANSIT_TIME;

        /**
         * @param length Length of the animation
         * @param id     Unique id for the animation
         */
        public Builder(int length, String id) {
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
         */
        public Builder marker(int time) {
            this.attackTime = Mth.clamp(time, 1, this.length);
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
            return new AnimatedAction(this.length, this.attackTime, this.id, this.clientIdentifier,
                    this.startTransition, this.endTransition, this.speed, this.shouldRunOut);
        }
    }
}

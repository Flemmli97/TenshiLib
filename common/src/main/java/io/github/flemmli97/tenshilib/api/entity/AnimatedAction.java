package io.github.flemmli97.tenshilib.api.entity;

import net.minecraft.util.Mth;

public class AnimatedAction {

    public static final AnimatedAction vanillaAttack = new AnimatedAction(20, 1, "vanilla");
    public static final AnimatedAction[] vanillaAttackOnly = {vanillaAttack};

    private final int length, attackTime, fadeTick;
    private final boolean shouldRunOut;
    private final String id, animationClient;
    private final float speed;

    private float ticker;

    /**
     * @param length Length of the animation in seconds
     * @param id     Unique id for the animation
     */
    public AnimatedAction(double length, String id) {
        this((int) Math.ceil(length * 20), id);
    }

    /**
     * @param length Length of the animation
     * @param id     Unique id for the animation
     */
    public AnimatedAction(int length, String id) {
        this(length, 1, id, id, 1, true, 0);
    }

    /**
     * @param length     Length of the animation in seconds
     * @param id         Unique id for the animation
     * @param attackTime A flag for various things e.g. when the entity should actually do damage
     *                   Example in a sword slash do the damage mid swing and not at the beginning.
     */
    public AnimatedAction(double length, double attackTime, String id) {
        this((int) Math.ceil(length * 20), (int) Math.ceil(attackTime * 20), id, id, 1, true, 0);
    }

    /**
     * @param length     Length of the animation
     * @param id         Unique id for the animation
     * @param attackTime A flag for various things e.g. when the entity should actually do damage
     *                   Example in a sword slash do the damage mid swing and not at the beginning.
     */
    public AnimatedAction(int length, int attackTime, String id) {
        this(length, attackTime, id, id, 1, true, 0);
    }

    /**
     * Use the builder {@link #builder}
     */
    private AnimatedAction(int length, int attackTime, String id, String animationClient, float speedMod, boolean shouldRunOut, int fadeTick) {
        this.speed = speedMod;
        this.length = Math.max(1, length);
        this.attackTime = Mth.clamp(attackTime, 1, this.length);
        this.id = id;
        this.animationClient = animationClient;
        this.shouldRunOut = shouldRunOut;
        this.fadeTick = fadeTick;
    }

    public static AnimatedAction copyOf(AnimatedAction animatedAction, String id) {
        return new AnimatedAction(animatedAction.length, animatedAction.attackTime, id, animatedAction.animationClient, animatedAction.speed, animatedAction.shouldRunOut, animatedAction.fadeTick);
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
        return new AnimatedAction(this.length, this.attackTime, this.id, this.animationClient, speed, this.shouldRunOut, this.fadeTick);
    }

    public boolean tick() {
        return this.tick(1);
    }

    public boolean tick(int offSet) {
        return (this.ticker += 1 * this.speed) >= (this.length + offSet) && this.shouldRunOut;
    }

    public float getSpeed() {
        return this.speed;
    }

    public boolean canAttack() {
        return this.isAtTick(this.attackTime);
    }

    public int getTick() {
        return (int) this.ticker;
    }

    public boolean isAtTick(double tick) {
        return this.isAtTick((int) Math.ceil(tick * 20));
    }

    /**
     * @return True if the current animation is at the given tick. Use this instead of #getTick() == tick since this respects animation speed
     */
    public boolean isAtTick(int tick) {
        return this.speedAdjustedTick() == tick;
    }

    public boolean isPastTick(double tick) {
        return this.isPastTick((int) Math.ceil(tick * 20));
    }

    /**
     * @return True if the current animation is past the given tick. Use this instead of #getTick() >= tick since this respects animation speed
     */
    public boolean isPastTick(int tick) {
        return this.speedAdjustedTick() >= tick;
    }

    public int speedAdjustedTick() {
        float adjusted = this.ticker / this.speed;
        int ceil = Mth.ceil(adjusted);
        return (ceil - adjusted) < 0.001 && (ceil - adjusted) > 0 ? ceil : (int) adjusted;
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

    public String getAnimationClient() {
        return this.animationClient;
    }

    /**
     * If false animation will only change if set manually. Else if it ticks out it gets set to null
     */
    public boolean shouldRunOut() {
        return this.shouldRunOut;
    }

    public int getFadeTick() {
        return this.fadeTick;
    }

    @Override
    public String toString() {
        return "ID: " + this.id + "; length: " + this.length + "; attackTime: " + this.attackTime + "; speed: " + this.speed;
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
        private int fadeTick;
        private boolean shouldRunOut = true;
        private final String id;
        private String animationClient;
        private float speed = 1;

        /**
         * @param length Length of the animation
         * @param id     Unique id for the animation
         */
        public Builder(int length, String id) {
            this.length = Math.max(1, length);
            this.id = id;
            this.animationClient = id;
        }

        /**
         * The animation to play on the client side. Normally same as id but for cases
         * where you have multiple attacks with same animation set this
         */
        public Builder withClientID(String id) {
            this.animationClient = id;
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
         * A delay used for when the animation changes. For interpolation stuff
         */
        public Builder changeDelay(int delay) {
            this.fadeTick = delay;
            return this;
        }

        public AnimatedAction build() {
            return new AnimatedAction(this.length, this.attackTime, this.id, this.animationClient, this.speed, this.shouldRunOut, this.fadeTick);
        }
    }
}

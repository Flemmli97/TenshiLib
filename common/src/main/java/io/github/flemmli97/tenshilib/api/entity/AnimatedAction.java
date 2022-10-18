package io.github.flemmli97.tenshilib.api.entity;

public class AnimatedAction {

    public static final AnimatedAction vanillaAttack = new AnimatedAction(20, 1, "vanilla");
    public static final AnimatedAction[] vanillaAttackOnly = {vanillaAttack};

    private final int length, attackTime;
    private final boolean shouldRunOut;
    private final String id, animationClient;
    private float ticker;
    private float speed = 1;

    public AnimatedAction(int length, String id) {
        this(length, 1, id, id);
    }

    public AnimatedAction(int length, int attackTime, String id) {
        this(length, attackTime, id, id);
    }

    /**
     * @param length          Length of the animation
     * @param attackTime      A flag for various things e.g. when the entity should actually do damage
     *                        Example in a sword slash do the damage mid swing and not at the beginning.
     * @param id              Unique id for the animation
     * @param animationClient The animation to play on the client side. Normally same as id but for cases
     *                        where you have multiple attacks with same animation set this
     */
    public AnimatedAction(int length, int attackTime, String id, String animationClient) {
        this(length, attackTime, id, animationClient, 1, true);
    }

    public AnimatedAction(int length, int attackTime, String id, String animationClient, float speedMod, boolean shouldRunOut) {
        this.speed = speedMod;
        this.length = length;
        this.attackTime = Math.max(1, attackTime);
        this.id = id;
        this.animationClient = animationClient;
        this.shouldRunOut = shouldRunOut;
    }

    public static AnimatedAction copyOf(AnimatedAction animatedAction, String id) {
        return new AnimatedAction(animatedAction.length, animatedAction.attackTime, id, animatedAction.animationClient, animatedAction.speed, animatedAction.shouldRunOut);
    }

    /**
     * @return Creates a new copy instance of the animation
     */
    public AnimatedAction create() {
        return new AnimatedAction(this.length, this.attackTime, this.id, this.animationClient, this.speed, this.shouldRunOut);
    }

    public boolean tick() {
        return (this.ticker += 1 * this.speed) >= this.length && this.shouldRunOut;
    }

    public boolean canAttack() {
        return this.ticker == this.attackTime;
    }

    public int getTick() {
        return (int) this.ticker;
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

    public boolean checkID(AnimatedAction other) {
        return other != null && this.id.equals(other.id);
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
}

package com.flemmli97.tenshilib.common.entity;

public class AnimatedAction {

    public static final AnimatedAction vanillaAttack = new AnimatedAction(20, 0, "vanilla");
    public static final AnimatedAction[] vanillaAttackOnly = new AnimatedAction[] {vanillaAttack};

    private final int length, attackTime;
    private final String id;
    private float ticker;
    private float speed = 1;

    public AnimatedAction(int length, int attackTime, String id) {
        this.length = length;
        this.attackTime = attackTime;
        this.id = id;
    }

    public AnimatedAction(int length, int attackTime, String id, boolean loops, float speedMod) {
        this(length, attackTime, id);
        this.speed = speedMod;
    }

    public AnimatedAction create() {
        return new AnimatedAction(this.length, this.attackTime, this.id);
    }

    public boolean tick() {
        return (this.ticker += 1 * this.speed) >= this.length;
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

    @Override
    public String toString() {
        return "ID: " + this.id + "; length: " + this.length + "; attackTime: " + this.attackTime + "; speed: " + this.speed;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof AnimatedAction)
            return this.toString().equals(o.toString());
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}

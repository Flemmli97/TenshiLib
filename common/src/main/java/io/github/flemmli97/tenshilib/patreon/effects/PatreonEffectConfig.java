package io.github.flemmli97.tenshilib.patreon.effects;

import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public abstract class PatreonEffectConfig {

    private static final Set<GuiElement> DEFAULT_ELEMENTS = Set.of(GuiElement.values());

    private final String id;
    public final int tier;

    public PatreonEffectConfig(String id) {
        this(id, 1);
    }

    public PatreonEffectConfig(String id, int tier) {
        this.id = id;
        this.tier = tier;
    }

    public abstract boolean locationAllowed(RenderLocation loc);

    public abstract RenderLocation defaultLoc();

    public Set<GuiElement> guiElements() {
        return DEFAULT_ELEMENTS;
    }

    public void tick(Player player) {
    }

    public int defaultColor() {
        return 0xFFFFFFFF;
    }

    public String id() {
        return this.id;
    }
}
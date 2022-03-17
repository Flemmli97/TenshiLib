package io.github.flemmli97.tenshilib.patreon;

import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class PatreonPlayerSetting {

    private PatreonEffects.PatreonEffectConfig conf;
    private RenderLocation renderLocation = RenderLocation.HAT;
    private boolean render = true;
    private int color;

    private final Player player;
    private boolean newSetting = true;

    public PatreonPlayerSetting(Player player) {
        this.player = player;
    }

    public void setToDefault(boolean forced) {
        if (forced || this.newSetting) {
            PatreonDataManager.PatreonPlayerInfo info = PatreonDataManager.get(this.player.getUUID().toString());
            if (info.tier() > 0) {
                this.setEffect(PatreonEffects.get(info.defaultEffect()));
                if (info.defaultRenderLocation() != null)
                    this.renderLocation = info.defaultRenderLocation();
                this.color = info.color();
            } else {
                this.conf = null;
            }
        }
    }

    public void setEffect(PatreonEffects.PatreonEffectConfig conf) {
        this.conf = conf;
        if (this.conf != null) {
            this.renderLocation = this.conf.defaultLoc();
            this.color = this.conf.defaultColor();
        }
    }

    public PatreonEffects.PatreonEffectConfig effect() {
        return this.conf;
    }

    public boolean shouldRender() {
        return this.render;
    }

    public RenderLocation getRenderLocation() {
        return this.renderLocation;
    }

    public int getColor() {
        return this.color;
    }

    public CompoundTag save(CompoundTag tag) {
        if (this.conf != null)
            tag.putString("Effect", this.conf.id());
        tag.putInt("Location", this.renderLocation.ordinal());
        tag.putBoolean("ShouldRender", this.render);
        tag.putInt("Color", this.color);
        return tag;
    }

    public void read(CompoundTag tag) {
        if (tag.contains("Effect"))
            this.setEffect(PatreonEffects.get(tag.getString("Effect")));
        RenderLocation loc = RenderLocation.values()[tag.getInt("Location")];
        if (this.conf != null) {
            if (this.conf.locationAllowed(loc))
                this.renderLocation = loc;
            else
                this.renderLocation = this.conf.defaultLoc();
        }
        this.render = tag.getBoolean("ShouldRender");
        this.color = tag.getInt("Color");
        this.newSetting = false;
    }

    public void read(C2SEffectUpdatePkt pkt, String id) {
        this.conf = PatreonEffects.get(id);
        RenderLocation loc = pkt.location;
        if (this.conf != null) {
            if (this.conf.locationAllowed(loc))
                this.renderLocation = loc;
            else
                this.renderLocation = this.conf.defaultLoc();
        }
        this.render = pkt.render;
        this.color = pkt.color;
    }
}

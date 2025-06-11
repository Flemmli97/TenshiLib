package io.github.flemmli97.tenshilib.client.model.animation.keyframe;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class SoundKeyFrame extends KeyFrameValue {

    private final String sound;
    private Holder<SoundEvent> soundEvent;

    public SoundKeyFrame(double startTick, String sound) {
        super(startTick);
        this.sound = sound;
    }

    @Nullable
    public Holder<SoundEvent> getSound(RegistryAccess access) {
        if (this.soundEvent == null) {
            try {
                ResourceLocation id = ResourceLocation.parse(this.sound);
                this.soundEvent = access.lookup(Registries.SOUND_EVENT)
                        .flatMap(r -> r.get(ResourceKey.create(Registries.SOUND_EVENT, id)))
                        .orElse(null);
            } catch (ResourceLocationException e) {
                TenshiLib.LOGGER.error("Invalid sound id {}", this.sound);
                TenshiLib.LOGGER.error(e);
            }
        }
        return this.soundEvent;
    }

    @Override
    public String toString() {
        return String.format("Marker Frame [@:%s, {%s}]", this.startTick, this.sound);
    }
}

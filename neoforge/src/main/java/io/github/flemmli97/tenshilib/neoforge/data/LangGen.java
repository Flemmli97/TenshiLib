package io.github.flemmli97.tenshilib.neoforge.data;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class LangGen extends LanguageProvider {

    public LangGen(PackOutput output) {
        super(output, TenshiLib.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("tenshilib.item.animation.select", "Selected %s");
        this.add("tenshilib.gui.animation", "Animation Selector");
        this.add("tenshilib.gui.save", "Save");

        this.add("tenshilib.patreon.title", "TenshiLib Patreon Customization");
        this.add("tenshilib.patreon.level.no", "Need to be in a world to use this");
        this.add("tenshilib.patreon.not", "Support me on patreon to use this. The features here are purely cosmetic");
        this.add("tenshilib.patreon.back", "Go back");
        this.add("tenshilib.patreon.save", "Save");
        this.add("tenshilib.patreon.id", "Patreon Effect");
        this.add("tenshilib.patreon.render", "Render Effect");
        this.add("tenshilib.patreon.location", "Render Location");
        this.add("tenshilib.patreon.color", "Color");
        this.add("tenshilib.patreon.slider.red", "Slider (Red)");
        this.add("tenshilib.patreon.slider.green", "Slider (Green)");
        this.add("tenshilib.patreon.slider.blue", "Slider (Blue)");
        this.add("tenshilib.patreon.slider.alpha", "Slider (Alpha)");
        this.add("tenshilib.patreon.location.CIRCLING", "Circling around");
        this.add("tenshilib.patreon.location.CIRCLINGREVERSE", "Circling around (reverse direction)");
        this.add("tenshilib.patreon.location.HAT", "Hat");
        this.add("tenshilib.patreon.location.HATNOARMOR", "Hat with no helmet");
        this.add("tenshilib.patreon.location.LEFTSHOULDER", "Left shoulder");
        this.add("tenshilib.patreon.location.RIGHTSHOULDER", "Right shoulder");
        this.add("tenshilib.patreon.location.BACK", "Back");
        this.add("tenshilib.patreon.id.megu_hat", "Megumin's Hat");
        this.add("tenshilib.patreon.id.chomusuke", "Chomusuke");
        this.add("tenshilib.patreon.id.cat", "Cat");
        this.add("tenshilib.patreon.id.halo", "Halo");
        this.add("tenshilib.patreon.id.halo_particle", "Halo (Particle)");
        this.add("tenshilib.patreon.id.cloud_particle", "Cloud (Particle)");
        this.add("tenshilib.patreon.id.enchanted", "Enchanted (Particle)");
        this.add("tenshilib.patreon.id.flaming_shield", "Flaming Shield (Particle)");
    }
}

package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.mixinhelper.ScreenWidgetAdder;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenAccessor implements ScreenWidgetAdder {

    @Shadow
    @Final
    List<Widget> renderables;
    @Shadow
    @Final
    List<GuiEventListener> children;
    @Shadow
    @Final
    List<NarratableEntry> narratables;

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> void widgetAdder(T widget) {
        this.renderables.add(widget);
        this.children.add(widget);
        this.narratables.add(widget);
    }
}

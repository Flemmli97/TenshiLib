package io.github.flemmli97.tenshilib.mixinhelper;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface ScreenWidgetAdder {

    <T extends GuiEventListener & Widget & NarratableEntry> void widgetAdder(T widget);
}

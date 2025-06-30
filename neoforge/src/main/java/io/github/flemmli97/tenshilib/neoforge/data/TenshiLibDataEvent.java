package io.github.flemmli97.tenshilib.neoforge.data;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TenshiLib.MODID, bus = EventBusSubscriber.Bus.MOD)
public class TenshiLibDataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent event) {
        event.getGenerator().addProvider(true, new LangGen(event.getGenerator().getPackOutput()));
    }
}

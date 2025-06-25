package io.github.flemmli97.tenshilib.neoforge.events;

import io.github.flemmli97.tenshilib.loader.event.AOEAttackHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.function.Consumer;

public class AOEAttackHandlerImpl implements AOEAttackHandler {

    @Override
    public void registerAOEEventHandler(AOEAttackEvent handler) {
        Consumer<AOEAttackEventForge> cons = event -> {
            if (handler.preventAttack(event.getEntity(), event.usedItem, event.attackList()))
                event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(cons);
    }

    @Override
    public boolean trigger(Player player, ItemStack stack, List<Entity> list) {
        return NeoForge.EVENT_BUS.post(new AOEAttackEventForge(player, stack, list)).isCanceled();
    }
}

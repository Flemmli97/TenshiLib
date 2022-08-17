package io.github.flemmli97.tenshilib.api.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemConfig<T extends IItemConfig<T>> extends IConfigValue<T> {

    Item getItem();

    ItemStack getStack();

    List<Item> getItemList();

    boolean hasList();

    /**
     * If the given stack matches this item config.
     * Note that it does not check for empty stack so empty stack + empty config will return true
     */
    boolean match(ItemStack stack);
}

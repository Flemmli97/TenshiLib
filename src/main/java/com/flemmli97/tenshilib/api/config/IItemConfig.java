package com.flemmli97.tenshilib.api.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IItemConfig<T extends IItemConfig<T>> extends IConfigValue<T> {

    Item getItem();

    ItemStack getStack();

    List<Item> getItemList();

    boolean hasList();

}

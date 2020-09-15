package com.flemmli97.tenshilib.api.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IItemConfig<T extends IItemConfig<T>> extends IConfigValue<T> {

    Item getItem();

    ItemStack getStack();

    NonNullList<ItemStack> getStackList();

    boolean hasList();

}

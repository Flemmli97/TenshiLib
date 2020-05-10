package com.flemmli97.tenshilib.common.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import scala.util.Random;

import java.util.Collection;
import java.util.List;

public class ItemUtil {

    private static List<Item> weapons = Lists.newArrayList();
    private static List<Item> armor = Lists.newArrayList();
    private static List<Item> tools = Lists.newArrayList();
    private static List<Item> boots = Lists.newArrayList();
    private static List<Item> legs = Lists.newArrayList();
    private static List<Item> chest = Lists.newArrayList();
    private static List<Item> helmet = Lists.newArrayList();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static boolean isItemBetter(ItemStack stack, ItemStack currentEquipped) {
        if(stack.getItem() instanceof ItemArmor){
            if(!(currentEquipped.getItem() instanceof ItemArmor) || EnchantmentHelper.hasBindingCurse(currentEquipped))
                return true;
            else if(currentEquipped.getItem() instanceof ItemArmor){
                ItemArmor itemarmor = (ItemArmor) stack.getItem();
                ItemArmor itemarmor1 = (ItemArmor) currentEquipped.getItem();

                if(itemarmor.damageReduceAmount == itemarmor1.damageReduceAmount){
                    return stack.getMetadata() > currentEquipped.getMetadata() || stack.hasTagCompound() && !currentEquipped.hasTagCompound();
                }else{
                    return itemarmor.damageReduceAmount > itemarmor1.damageReduceAmount;
                }
            }
        }else if(stack.getItem() instanceof ItemBow){
            if(currentEquipped.isEmpty())
                return true;
            if(currentEquipped.getItem() instanceof ItemBow){
                int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                int power2 = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, currentEquipped);
                return power > power2;
            }
        }else{
            if(currentEquipped.isEmpty())
                return true;
            return damage(stack) > damage(currentEquipped);
        }
        return false;
    }

    public static double damage(ItemStack stack) {
        double dmg = 0;
        Collection<AttributeModifier> atts = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
        for(AttributeModifier mod : atts){
            if(mod.getOperation() == 0)
                dmg += mod.getAmount();
        }
        double value = dmg;
        for(AttributeModifier mod : atts){
            if(mod.getOperation() == 1)
                value += dmg * mod.getAmount();
        }
        for(AttributeModifier mod : atts){
            if(mod.getOperation() == 2)
                value *= 1 + mod.getAmount();
        }
        int sharp = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        if(sharp > 0)
            dmg += sharp * 0.5 + 0.5;
        return SharedMonsterAttributes.ATTACK_DAMAGE.clampValue(value);
    }

    @SuppressWarnings("incomplete-switch")
    public static void initItemLists() {
        ForgeRegistries.ITEMS.forEach(entry -> {
            if(entry instanceof ItemArmor){
                armor.add(entry);
                ItemArmor armor = (ItemArmor) entry;
                switch(armor.armorType){
                    case CHEST:
                        chest.add(entry);
                        break;
                    case FEET:
                        boots.add(entry);
                        break;
                    case HEAD:
                        helmet.add(entry);
                        break;
                    case LEGS:
                        legs.add(entry);
                        break;
                }
            }
            if(entry instanceof ItemTool)
                tools.add(entry);
            if(entry instanceof ItemSword || entry instanceof ItemTool || isCustomWeapon(entry))
                weapons.add(entry);
        });
    }

    /**
     * @param slot slot offhand for tools
     * @return
     */
    public static List<Item> getList(EntityEquipmentSlot slot) {
        switch(slot){
            case CHEST:
                return ImmutableList.copyOf(chest);
            case FEET:
                return ImmutableList.copyOf(boots);
            case HEAD:
                return ImmutableList.copyOf(helmet);
            case LEGS:
                return ImmutableList.copyOf(legs);
            case MAINHAND:
                return ImmutableList.copyOf(weapons);
            case OFFHAND:
                return ImmutableList.copyOf(tools);
        }
        return ImmutableList.copyOf(Lists.newArrayList());
    }

    public static ItemStack getRandomFromSlot(EntityEquipmentSlot slot) {
        Random rand = new Random();
        switch(slot){
            case CHEST:
                return new ItemStack(chest.get(rand.nextInt(chest.size())));
            case FEET:
                return new ItemStack(boots.get(rand.nextInt(boots.size())));
            case HEAD:
                return new ItemStack(helmet.get(rand.nextInt(helmet.size())));
            case LEGS:
                return new ItemStack(legs.get(rand.nextInt(legs.size())));
            case MAINHAND:
                return new ItemStack(weapons.get(rand.nextInt(weapons.size())));
            default:
                break;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getRandomTool() {
        return new ItemStack(tools.get(new Random().nextInt(tools.size())));
    }

    /**
     * From Forges CraftingHandler, but returns the itemstacks stackTagCompound
     */
    public static NBTTagCompound stackCompoundFromJson(JsonObject json) {
        if(json.has("nbt")){
            try{
                JsonElement element = json.get("nbt");
                NBTTagCompound nbt;
                if(element.isJsonObject()){
                    nbt = JsonToNBT.getTagFromJson(GSON.toJson(element));
                }else{
                    nbt = JsonToNBT.getTagFromJson(element.getAsString());
                }
                NBTTagCompound tmp = new NBTTagCompound();
                if(nbt.hasKey("ForgeCaps")){
                    tmp.setTag("ForgeCaps", nbt.getTag("ForgeCaps"));
                    nbt.removeTag("ForgeCaps");
                }
                tmp.setTag("tag", nbt);
                return tmp;
            }catch(NBTException e){
                throw new JsonSyntaxException("Invalid NBT Entry: " + e.toString());
            }
        }
        return null;
    }

    /**
     * Tests, if the players inventory has enough space for the itemstack without actually adding it to the inventory
     */
    public static boolean hasSpace(EntityPlayer player, ItemStack stack) {
        if(stack.isEmpty()){
            return false;
        }
        InventoryPlayer inv = player.inventory;
        stack = stack.copy();
        for(ItemStack invStack : inv.mainInventory){
            if(invStack.isEmpty()){
                stack.setCount(stack.getCount() - stack.getMaxStackSize());
            }else if(invStack.getCount() < invStack.getMaxStackSize() && areItemsStackable(stack, invStack)){
                int sub = invStack.getMaxStackSize() - invStack.getCount();
                stack.setCount(stack.getCount() - sub);
            }
            if(stack.getCount() <= 0){
                break;
            }
        }
        return stack.getCount() <= 0;
    }

    public static boolean areItemsStackable(ItemStack stack1, ItemStack stack2) {
        return (stack1.isEmpty() && stack2.isEmpty()) || (!stack1.isEmpty() && !stack2.isEmpty() && stack1.getItem() == stack2.getItem()
                && stack1.getMetadata() == stack2.getMetadata() && ((!stack1.hasTagCompound() && !stack2.hasTagCompound()) || (stack1.hasTagCompound()
                        && (stack1.getTagCompound().equals(stack2.getTagCompound()) && stack1.areCapsCompatible(stack2)))));
    }

    private static boolean isCustomWeapon(Item item) {
        //if(CommonProxy.isFateLoaded && item instanceof ClassSpear)
        //	return true;
        return false;
    }
}

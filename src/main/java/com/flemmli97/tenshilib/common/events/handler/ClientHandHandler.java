package com.flemmli97.tenshilib.common.events.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ClientHandHandler {

    private static final ClientHandHandler instance;
    static{
        instance = new ClientHandHandler();
    }

    public static ClientHandHandler getInstance() {
        return instance;
    }

    private int lastSwingOff, lastSwingMain;

    private float prevEquippedProgressOffHand, prevEquippedProgressMainHand;
    private float equippedProgressOffHand, equippedProgressMainHand;
    private ItemStack itemStackMainHand = ItemStack.EMPTY;
    private EnumHand hand = EnumHand.OFF_HAND;

    public float equipProgress(EnumHand hand, float partialTicks) {
        if(hand == EnumHand.OFF_HAND)
            return 1.0F - (this.prevEquippedProgressOffHand + (this.equippedProgressOffHand - this.prevEquippedProgressOffHand) * partialTicks);
        return 1.0F - (this.prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * partialTicks);
    }

    public EnumHand nextHand() {
        if(this.hand == EnumHand.MAIN_HAND)
            this.hand = EnumHand.OFF_HAND;
        else
            this.hand = EnumHand.MAIN_HAND;
        return this.hand;
    }

    public EnumHand currentHand() {
        return this.hand;
    }

    public void resetSwing() {
        if(this.nextHand() == EnumHand.OFF_HAND)
            this.lastSwingOff = 0;
        else
            this.lastSwingMain = 0;
    }

    public void updateEquippedItem() {
        ++this.lastSwingOff;
        ++this.lastSwingMain;

        this.prevEquippedProgressMainHand = this.equippedProgressMainHand;
        this.prevEquippedProgressOffHand = this.equippedProgressOffHand;
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
        ItemStack stack = entityplayersp.getHeldItemMainhand();

        if(entityplayersp.isRowingBoat()){
            this.equippedProgressMainHand = MathHelper.clamp(this.equippedProgressMainHand - 0.4F, 0.0F, 1.0F);
            this.equippedProgressOffHand = MathHelper.clamp(this.equippedProgressOffHand - 0.4F, 0.0F, 1.0F);
        }else{
            float main = this.getCooledAttackStrength(entityplayersp, this.lastSwingMain);
            float off = this.getCooledAttackStrength(entityplayersp, this.lastSwingOff);

            boolean requipM = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackMainHand, stack,
                    entityplayersp.inventory.currentItem);

            if((!requipM && !Objects.equals(this.itemStackMainHand, stack)) || this.changed(stack)){
                this.itemStackMainHand = stack;
                this.lastSwingMain = 0;
                this.lastSwingOff = 0;
            }

            this.equippedProgressMainHand += MathHelper.clamp((!requipM ? main * main * main : 0.0F) - this.equippedProgressMainHand, -0.4F, 0.4F);
            this.equippedProgressOffHand += MathHelper.clamp((!requipM ? off * off * off : 0) - this.equippedProgressOffHand, -0.4F, 0.4F);
        }

        if(this.equippedProgressMainHand < 0.1F || this.equippedProgressOffHand < 0.1F){
            this.itemStackMainHand = stack;
        }
    }

    private boolean changed(ItemStack stack) {
        if(!ItemStack.areItemStacksEqual(this.itemStackMainHand, stack)){
            return !ItemStack.areItemsEqualIgnoreDurability(this.itemStackMainHand, stack);
        }
        return false;
    }

    public float getCooledAttackStrength(EntityPlayer player, int tick) {
        return MathHelper.clamp((tick + 1) / player.getCooldownPeriod(), 0.0F, 1.0F);
    }
}

package com.flemmli97.tenshilib.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Keyboard;

import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.PacketStructurePiece;
import com.flemmli97.tenshilib.common.world.structure.GenerationType;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class GuiStructure extends GuiScreen {

    private TileStructurePiece tile;
    private GuiButton mirror, rot, genType, reset, save, addRes, changePage;
    private GuiTextField offX, offY, offZ;
    private List<Triple<GuiTextField, GuiTextField, GuiButton>> resources = Lists.newArrayList();
    private int page;

    public GuiStructure(Minecraft minecraft, TileStructurePiece tileEntity) {
        this.mc = minecraft;
        this.tile = tileEntity;
        if(!this.mc.player.canUseCommand(2, ""))
            this.mc.displayGuiScreen(null);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.resources.clear();
        this.buttonList.add(this.changePage = new GuiButton(0, this.width / 2 + 153 - (this.page == 0 ? this.editWidth() : this.backWidth()),
                this.page == 0 ? 80 : 40, this.page == 0 ? this.editWidth() : this.backWidth(), 20,
                this.page == 0 ? I18n.format("structure_piece.edit") : I18n.format("structure_piece.back")));
        this.offX = new GuiTextField(0, this.mc.fontRenderer, this.width / 2 - 153, 160, 80, 20);
        this.offX.setText("" + this.tile.offSet().getX());
        this.offY = new GuiTextField(1, this.mc.fontRenderer, this.width / 2 - 73, 160, 80, 20);
        this.offY.setText("" + this.tile.offSet().getY());
        this.offZ = new GuiTextField(2, this.mc.fontRenderer, this.width / 2 + 7, 160, 80, 20);
        this.offZ.setText("" + this.tile.offSet().getZ());
        if(this.page == 0){
            this.buttonList.add(this.rot = new GuiButton(0, this.width / 2 - 153, 40, 40, 20, this.rotationString(this.tile.currentrotation())));
            this.buttonList.add(this.mirror = new GuiButton(1, this.width / 2 - 153, 80, 40, 20, this.mirrorString(this.tile.currentMirror())));
            this.buttonList.add(this.genType = new GuiButton(2, this.width / 2 - 153, 120, 40, 20, this.genString(this.tile.generationType())));
            this.buttonList.add(this.reset = new GuiButton(3, this.width / 2 + 153 - 40, 40, 40, 20, "Reinit"));
            this.reset.enabled = this.tile.initialized();
            this.buttonList.add(this.save = new GuiButton(4, this.width / 2 - 153, 200, 60, 20, "Save"));
            this.offX.setEnabled(true);
            this.offY.setEnabled(true);
            this.offZ.setEnabled(true);
            this.offX.setVisible(true);
            this.offY.setVisible(true);
            this.offZ.setVisible(true);
            this.resources.clear();
        }else if(this.page == 1){
            this.offX.setEnabled(false);
            this.offY.setEnabled(false);
            this.offZ.setEnabled(false);
            this.offX.setVisible(false);
            this.offY.setVisible(false);
            this.offZ.setVisible(false);
            int offSet = -1;
            for(Entry<Float, List<ResourceLocation>> e : this.tile.entries()){
                for(ResourceLocation resource : e.getValue()){
                    offSet++;
                    GuiTextField res = new GuiTextField(offSet, this.mc.fontRenderer, this.width / 2 - 153, 70 + 23 * offSet, 180, 20);
                    res.setText(resource != null ? resource.toString() : "");
                    GuiTextField f = new GuiTextField(offSet, this.mc.fontRenderer, this.width / 2 + 153 - 122, 70 + 23 * offSet, 98, 20);
                    f.setText(e.getKey().toString());
                    GuiButton button = new GuiButton(offSet, this.width / 2 + 153 - 20, 70 + 23 * offSet, 20, 20, "X");
                    this.resources.add(Triple.of(res, f, button));
                }
            }
            this.buttonList.add(this.addRes = new GuiButton(5, this.width / 2 - 153, 40, 20, 20, "+"));
        }
    }

    private int editWidth() {
        return this.mc.fontRenderer.getStringWidth(I18n.format("structure_piece.edit")) + 10;
    }

    private int backWidth() {
        return this.mc.fontRenderer.getStringWidth(I18n.format("structure_piece.back")) + 10;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if(this.page == 0){
            this.drawString(this.fontRenderer, I18n.format("structure_piece.rotation"), this.width / 2 - 153, 30, 10526880);
            this.drawString(this.fontRenderer, I18n.format("structure_piece.reinit"),
                    this.width / 2 + 153 - this.fontRenderer.getStringWidth(I18n.format("structure_piece.reinit")), 30, 10526880);

            this.drawString(this.fontRenderer, I18n.format("structure_piece.mirror"), this.width / 2 - 153, 70, 10526880);

            this.drawString(this.fontRenderer, I18n.format("structure_piece.ground"), this.width / 2 - 153, 110, 10526880);

            this.drawString(this.fontRenderer, I18n.format("structure_piece.offset"), this.width / 2 - 153, 150, 10526880);
        }
        this.offX.drawTextBox();

        this.offY.drawTextBox();

        this.offZ.drawTextBox();
        if(this.page == 1){
            this.resources.forEach(triple -> {
                triple.getLeft().drawTextBox();
                triple.getMiddle().drawTextBox();
                triple.getRight().drawButton(this.mc, mouseX, mouseY, partialTicks);
            });
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean removed, doChangePage;

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.offX.mouseClicked(mouseX, mouseY, mouseButton);
        this.offY.mouseClicked(mouseX, mouseY, mouseButton);
        this.offZ.mouseClicked(mouseX, mouseY, mouseButton);
        this.resources.removeIf(new Predicate<Triple<GuiTextField, GuiTextField, GuiButton>>() {

            @Override
            public boolean test(Triple<GuiTextField, GuiTextField, GuiButton> triple) {
                triple.getLeft().mouseClicked(mouseX, mouseY, mouseButton);
                triple.getMiddle().mouseClicked(mouseX, mouseY, mouseButton);
                return GuiStructure.this.deleteTriple(triple.getRight(), mouseX, mouseY);
            }
        });
        if(this.removed){
            this.orderResources();
            this.removed = false;
        }
        if(this.doChangePage){
            this.doChangePage = false;
            this.page = this.page == 0 ? 1 : 0;
            this.initGui();
        }
    }

    private boolean deleteTriple(GuiButton guibutton, int mouseX, int mouseY) {
        if(guibutton.mousePressed(this.mc, mouseX, mouseY)){
            net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(
                    this, guibutton, this.buttonList);
            if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                return false;
            guibutton = event.getButton();
            this.selectedButton = guibutton;
            guibutton.playPressSound(this.mc.getSoundHandler());
            if(this.equals(this.mc.currentScreen))
                net.minecraftforge.common.MinecraftForge.EVENT_BUS
                        .post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.buttonList));
            this.removed = true;
            return true;
        }
        return false;
    }

    private void orderResources() {
        for(int i = 0; i < this.resources.size(); i++){
            Triple<GuiTextField, GuiTextField, GuiButton> triple = this.resources.get(i);
            triple.getLeft().y = 70 + 23 * i;
            triple.getMiddle().y = 70 + 23 * i;
            triple.getRight().y = 70 + 23 * i;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean typed = false;
        if(this.offX.getVisible() && (isNumber(this.offX.getText(), typedChar) || keyCode == 14)){
            typed = this.offX.textboxKeyTyped(typedChar, keyCode);
        }

        if(this.offY.getVisible() && (isNumber(this.offX.getText(), typedChar) || keyCode == 14)){
            typed = this.offY.textboxKeyTyped(typedChar, keyCode);
        }

        if(this.offZ.getVisible() && (isNumber(this.offX.getText(), typedChar) || keyCode == 14)){
            typed = this.offZ.textboxKeyTyped(typedChar, keyCode);
        }
        for(Triple<GuiTextField, GuiTextField, GuiButton> triple : this.resources){
            typed = triple.getLeft().textboxKeyTyped(typedChar, keyCode);
            if(floatTest(triple.getMiddle().getText(), typedChar) || keyCode == 14)
                typed = triple.getMiddle().textboxKeyTyped(typedChar, keyCode);
        }
        if(!typed)
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == this.mirror){
            switch(this.tile.currentMirror()){
                case FRONT_BACK:
                    this.tile.mirror(Mirror.NONE);
                    break;
                case LEFT_RIGHT:
                    this.tile.mirror(Mirror.FRONT_BACK);
                    break;
                case NONE:
                    this.tile.mirror(Mirror.LEFT_RIGHT);
                    break;
            }
            this.mirror.displayString = this.mirrorString(this.tile.currentMirror());
        }
        if(button == this.rot){
            switch(this.tile.currentrotation()){
                case CLOCKWISE_180:
                    this.tile.rotate(Rotation.COUNTERCLOCKWISE_90);
                    break;
                case CLOCKWISE_90:
                    this.tile.rotate(Rotation.CLOCKWISE_180);
                    break;
                case COUNTERCLOCKWISE_90:
                    this.tile.rotate(Rotation.NONE);
                    break;
                case NONE:
                    this.tile.rotate(Rotation.CLOCKWISE_90);
                    break;
            }
            this.rot.displayString = this.rotationString(this.tile.currentrotation());
        }
        if(button == this.genType){
            switch(this.tile.generationType()){
                case FLOATING:
                    this.tile.setGenerationType(GenerationType.REPLACEBELOW);
                    break;
                case GROUNDALIGNED:
                    this.tile.setGenerationType(GenerationType.FLOATING);
                    break;
                case REPLACEBELOW:
                    this.tile.setGenerationType(GenerationType.GROUNDALIGNED);
                    break;
            }
            this.genType.displayString = this.genString(this.tile.generationType());
        }
        if(button == this.reset){
            this.tile.reset();
            this.reset.enabled = false;
        }
        if(button == this.save){
            this.tile.setOffSet(new BlockPos(Integer.parseInt(this.offX.getText()), Integer.parseInt(this.offY.getText()),
                    Integer.parseInt(this.offZ.getText())));
            PacketHandler.sendToServer(new PacketStructurePiece(this.tile.writeToNBT(new NBTTagCompound()), false));
        }
        if(button == this.changePage){
            if(this.page == 1){
                this.tile.clearStructureNames();
                this.resources.forEach(triple -> {
                    if(!triple.getMiddle().getText().isEmpty())
                        this.tile.addStructureName(Float.parseFloat(triple.getMiddle().getText()),
                                triple.getLeft().getText().isEmpty() ? null : new ResourceLocation(triple.getLeft().getText()));
                });
            }else
                this.tile.setOffSet(new BlockPos(Integer.parseInt(this.offX.getText()), Integer.parseInt(this.offY.getText()),
                        Integer.parseInt(this.offZ.getText())));
            this.doChangePage = true;
        }
        if(button == this.addRes && this.resources.size() < 8){
            int offSet = this.resources.size();
            GuiTextField res = new GuiTextField(offSet, this.mc.fontRenderer, this.width / 2 - 153, 70 + 23 * offSet, 180, 20);
            GuiTextField f = new GuiTextField(offSet, this.mc.fontRenderer, this.width / 2 + 153 - 122, 70 + 23 * offSet, 98, 20);
            GuiButton delete = new GuiButton(offSet, this.width / 2 + 153 - 20, 70 + 23 * offSet, 20, 20, "X");
            this.resources.add(Triple.of(res, f, delete));
        }
    }

    @Override
    public void onGuiClosed() {
        PacketHandler.sendToServer(new PacketStructurePiece(this.tile.writeToNBT(new NBTTagCompound()), true));
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private boolean isNumber(String s, char c) {
        return Character.isDigit(c) || (s.isEmpty() && c == '-');
    }

    private boolean floatTest(String s, char c) {
        if(s.length() == 1 && s.charAt(0) == '1')
            return false;
        if(s.isEmpty())
            return c == '1' || c == '0';
        return s.length() == 1 ? c == '.' : Character.isDigit(c);
    }

    public String mirrorString(Mirror mirror) {
        switch(mirror){
            case FRONT_BACK:
                return "^ v";
            case LEFT_RIGHT:
                return "< >";
            case NONE:
                return "|";
        }
        return "";
    }

    public String rotationString(Rotation rot) {
        switch(rot){
            case CLOCKWISE_180:
                return "180°";
            case CLOCKWISE_90:
                return "90°";
            case COUNTERCLOCKWISE_90:
                return "270°";
            case NONE:
                return "0°";
        }
        return "";
    }

    public String genString(GenerationType type) {
        switch(type){
            case FLOATING:
                return "---";
            case GROUNDALIGNED:
                return "~~~";
            case REPLACEBELOW:
                return "___";
        }
        return "";
    }
}

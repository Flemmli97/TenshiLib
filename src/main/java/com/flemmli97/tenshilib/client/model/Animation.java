package com.flemmli97.tenshilib.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.javahelper.ArrayUtils;
import com.flemmli97.tenshilib.common.javahelper.MathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;

/**
 * Reads an extracted tabula animation json from file. Uses and identifierMap for matching the ModelRenderer with
 * tabulas identifiers. The name needs to match the ModelRendere's field name
 */
public class Animation {

    private Map<ModelRenderer, ArrayList<AnimationComponent>> map = Maps.newHashMap();
    private static final Gson gson = new Gson();
    private int length;
    private IResetModel model;
    private boolean doReset;

    public Animation(ModelBase model, ResourceLocation res) {
        this(model, res, true);
    }

    public Animation(ModelBase model, ResourceLocation res, boolean reset) {
        InputStream input = Loader.class.getResourceAsStream("/assets/" + res.getResourceDomain() + "/" + res.getResourcePath());
        if(input == null){
            TenshiLib.logger.error("Couldn't find animation: " + res);
            return;
        }
        try{
            JsonObject obj = gson.getAdapter(JsonObject.class).read(gson.newJsonReader(new InputStreamReader(input)));
            JsonObject idMap = (JsonObject) obj.get("identifierMap");
            JsonObject animSets = (JsonObject) obj.get("sets");
            for(Field field : model.getClass().getFields()){
                if(ModelRenderer.class.isAssignableFrom(field.getType()) && idMap.has(field.getName())){
                    String id = idMap.get(field.getName()).getAsString();
                    if(animSets.has(id)){
                        JsonArray arr = animSets.getAsJsonArray(id);
                        arr.forEach(element -> {
                            try{
                                AnimationComponent comp = new AnimationComponent(model, (JsonObject) element);
                                this.map.merge((ModelRenderer) field.get(model), Lists.newArrayList(comp), (old, val) -> {
                                    old.addAll(val);
                                    old.sort(null);
                                    return old;
                                });
                            }catch(Exception e){
                                TenshiLib.logger.error("Error parsing animation component: {}", element);
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        this.doReset = reset;
        if(model instanceof IResetModel)
            this.model = (IResetModel) model;
    }

    //ticker ticks up
    public void animate(int ticker, float partialTicks) {
        int tick = ticker % this.getLength();
        if(this.model != null && this.doReset)
            this.model.resetModel();
        this.map.entrySet().forEach(entry -> {
            entry.getValue().forEach(comp -> {
                comp.animate(entry.getKey(), tick, partialTicks);
            });
        });
    }

    public int getLength() {
        if(this.length == 0)
            this.map.values().forEach(list -> {
                list.forEach(comp -> {
                    if(comp.startKey + comp.length > this.length){
                        this.length = comp.startKey + comp.length;
                    }
                });
            });
        return this.length;
    }

    public void addComponent(ModelRenderer model, AnimationComponent component) {
        this.map.merge(model, Lists.newArrayList(component), (old, val) -> {
            old.addAll(val);
            old.sort(null);
            return old;
        });
        this.map.values().forEach(list -> {
            list.forEach(comp -> {
                if(comp.startKey + comp.length > this.length){
                    this.length = comp.startKey + comp.length;
                }
            });
        });
    }

    /**
     * https://github.com/iChun/iChunUtil/blob/master/src/main/java/me/ichun/mods/ichunutil/common/module/tabula/project/components/AnimationComponent.java
     */
    protected static class AnimationComponent implements Comparable<AnimationComponent> {

        public double[] posChange = new double[3];
        public double[] rotChange = new double[3];

        //a.k.a starting points
        public double[] posOffset = new double[3];
        public double[] rotOffset = new double[3];

        //Use of this?
        //public double opacityChange = 0.0D;
        //public double opacityOffset = 0.0D;
        //public boolean hidden;
        //public double[] scaleChange = new double[3];
        //public double[] scaleOffset = new double[3];

        //Add support for this?
        //public ArrayList<double[]> progressionCoords;
        //public transient PolynomialFunctionLagrangeForm progressionCurve;

        public int length;
        public int startKey;
        private String name;

        public AnimationComponent(ModelBase model, JsonObject obj) {
            //convert to rad
            JsonArray posChangeJson = obj.getAsJsonArray("posChange");
            for(int i = 0; i < 3; i++){
                double change = posChangeJson.get(i).getAsDouble();
                if(change != 0 && !(model instanceof IResetModel))
                    throw new IllegalArgumentException(
                            "Model needs to implement IResetModel. Else changes to rotation points will mess up the model during animation");
                this.posChange[i] = change;
            }
            JsonArray rotChangeJson = obj.getAsJsonArray("rotChange");
            for(int i = 0; i < 3; i++)
                this.rotChange[i] = MathUtils.degToRad((float) rotChangeJson.get(i).getAsDouble());
            JsonArray posOffJson = obj.getAsJsonArray("posOffset");
            for(int i = 0; i < 3; i++){
                double off = posOffJson.get(i).getAsDouble();
                if(off != 0 && !(model instanceof IResetModel))
                    throw new IllegalArgumentException(
                            "Model needs to implement IResetModel. Else changes to rotation points will mess up the model during animation");
                this.posOffset[i] = off;
            }
            JsonArray rotOffJson = obj.getAsJsonArray("rotOffset");
            for(int i = 0; i < 3; i++)
                this.rotOffset[i] = MathUtils.degToRad((float) rotOffJson.get(i).getAsDouble());
            this.length = obj.get("length").getAsInt();
            this.startKey = obj.get("startKey").getAsInt();
            this.name = obj.get("name").getAsString();
        }

        public void animate(ModelRenderer model, int ticker, float partialTicks) {
            float actualTick = Math.max(ticker - 1 + partialTicks, 0);
            float prog = MathHelper.clamp((actualTick - startKey) / (float) length, 0F, 1F);
            if(ticker >= this.startKey){
                model.rotationPointX += this.posOffset[0];
                model.rotationPointY += this.posOffset[1];
                model.rotationPointZ += this.posOffset[2];
                model.rotateAngleX += this.rotOffset[0];
                model.rotateAngleY += this.rotOffset[1];
                model.rotateAngleZ += this.rotOffset[2];
            }
            model.rotationPointX += this.posChange[0] * prog;
            model.rotationPointY += this.posChange[1] * prog;
            model.rotationPointZ += this.posChange[2] * prog;
            model.rotateAngleX += this.rotChange[0] * prog;
            model.rotateAngleY += this.rotChange[1] * prog;
            model.rotateAngleZ += this.rotChange[2] * prog;
        }
        /*
         * public void animate(ModelRenderer info, float time) { float prog = MathHelper.clamp((time - startKey) /
         * (float)length, 0F, 1F); float mag = prog; if(getProgressionCurve() != null) { mag =
         * MathHelper.clamp((float)getProgressionCurve().value(prog), 0.0F, 1.0F); } if(time >= startKey) { for(int i = 0; i <
         * 3; i++) { info.position[i] += posOffset[i]; info.rotation[i] += rotOffset[i]; info.scale[i] += scaleOffset[i]; }
         * info.opacity += opacityOffset; } for(int i = 0; i < 3; i++) { info.position[i] += posChange[i] * mag;
         * info.rotation[i] += rotChange[i] * mag; info.scale[i] += scaleChange[i] * mag; } info.opacity += opacityChange * mag;
         * }
         * 
         * public double getProgressionFactor(double progression) { if(progressionCurve == null) { return progression; } return
         * progressionCurve.value(progression); }
         * 
         * public void createProgressionCurve() { if(progressionCoords != null) { double[] xes = new
         * double[progressionCoords.size() + 2]; double[] yes = new double[progressionCoords.size() + 2];
         * 
         * xes[0] = yes[0] = 0; xes[1] = yes[1] = 1;
         * 
         * for(int i = 0; i < progressionCoords.size(); i++) { xes[2 + i] = progressionCoords.get(i)[0]; yes[2 + i] =
         * progressionCoords.get(i)[1]; }
         * 
         * progressionCurve = new PolynomialFunctionLagrangeForm(xes, yes); } }
         * 
         * public PolynomialFunctionLagrangeForm getProgressionCurve() { if(progressionCoords != null && progressionCurve ==
         * null) { createProgressionCurve(); } return progressionCurve; }
         */

        @Override
        public String toString() {
            return this.name + ":{PosOffset:[" + ArrayUtils.arrayToString(this.posOffset) + "],RotOffset:[" + ArrayUtils.arrayToString(this.rotOffset)
                    + "],PosChange:[" + ArrayUtils.arrayToString(this.posChange) + "],RotChange:[" + ArrayUtils.arrayToString(this.rotChange) + "]}";
        }

        @Override
        public int compareTo(AnimationComponent o) {
            return Integer.compare(o.startKey, this.startKey);
        }
    }
}

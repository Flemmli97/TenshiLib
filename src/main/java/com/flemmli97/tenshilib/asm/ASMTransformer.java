package com.flemmli97.tenshilib.asm;

import com.google.common.collect.Maps;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;
import java.util.Map;

public class ASMTransformer implements IClassTransformer {

    private static Map<String, Pair<Transform, Method>> patches = Maps.newHashMap();

    private interface Transform {

        public void apply(ClassNode clss, MethodNode method);
    }

    static{
        patches.put("net.minecraft.client.Minecraft",
                Pair.of(ASMTransformer::patchClickMouse, new Method("clickMouse", "func_147116_af", "aA", "()V", "()V")));
        //patches.put("net.minecraft.client.renderer.entity.RenderLivingBase", patchRenderLivingBase());
        //classMethod.put("net.minecraft.client.renderer.entity.RenderLivingBase", new Method("doRender", "func_76986_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", "(Lvp;DDDFF)V"));
        patches.put("net.minecraft.client.model.ModelBiped", Pair.of(ASMTransformer::patchModelPlayer,
                new Method("render", "func_78088_a", "a", "(Lnet/minecraft/entity/Entity;FFFFFF)V", "(Lvg;FFFFFF)V")));
        patches.put("net.minecraft.pathfinding.PathNavigate", Pair.of(ASMTransformer::patchPathNavigate,
                new Method("<init>", "<init>", "<init>", "(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/world/World;)V", "(Lvq;Lamu;)V")));
        patches.put("net.minecraft.client.renderer.entity.layers.LayerHeldItem", Pair.of(ASMTransformer::layerHeldItem,
                new Method("doRenderLayer", "func_177141_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFFF)V", "(Lvp;FFFFFFF)V")));
    }

    protected static void asmDebug(String debug) {
        System.out.println("[TenshiCore]: " + debug);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ASMLoader.asmLoaded = true;
        if(patches.containsKey(transformedName)){
            Pair<Transform, Method> pair = patches.get(transformedName);
            return transform(basicClass, pair.getRight(), pair.getLeft());
        }
        return basicClass;
    }

    private static byte[] transform(byte[] clss, Method m, Transform transform) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(clss);
        classReader.accept(classNode, 0);
        try{
            for(MethodNode method : classNode.methods){
                if(m.matches(method)){
                    transform.apply(classNode, method);
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(writer);
                    return writer.toByteArray();
                }
            }
            throw new ASMException("No Method found", m);
        }catch(ASMException e){
            //A more visible alert, if some name changes and it cant find the method
            asmDebug("MethodNode not found for " + e.getMethod());
            e.printStackTrace();
        }
        return clss;
    }

    protected static void testNonNull(AbstractInsnNode node, Method m) {
        try{
            if(node == null)
                throw new ASMException("Instruction not found", m);
        }catch(ASMException e){
            asmDebug("Instruction not found for " + e.getMethod());
            e.printStackTrace();
        }
    }

    /**Replace #attackEntity and #swingArm in {@link net.minecraft.client.Minecraft#clickMouse} */
    private static void patchClickMouse(ClassNode clss, MethodNode method) {
        asmDebug("Patching Minecraft.clickMouse");
        Iterator<AbstractInsnNode> it = method.instructions.iterator();
        AbstractInsnNode node = null;
        AbstractInsnNode attackEntity = null;
        AbstractInsnNode armSwing = null;
        Method attackEntityMethod = new Method("attackEntity", "func_78764_a", "a",
                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V", "(Laed;Lvg;)V");
        Method armSwingMethod = new Method("swingArm", "func_184609_a", "a", "(Lnet/minecraft/util/EnumHand;)V", "(Lub;)V");
        while(it.hasNext()){
            node = it.next();
            if(node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode){
                MethodInsnNode dyn = (MethodInsnNode) node;
                //playerController.attackEntity
                if(attackEntityMethod.matches(dyn))
                    attackEntity = dyn;
                //player.swingArm
                if(armSwingMethod.matches(dyn))
                    armSwing = dyn;
            }
        }
        testNonNull(attackEntity, attackEntityMethod);
        testNonNull(armSwing, armSwingMethod);

        if(ASMLoader.isDeobfEnvironment()){
            method.instructions.insert(attackEntity, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods",
                    "attackEntityClient",
                    "(Lnet/minecraft/client/multiplayer/PlayerControllerMP;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V",
                    false));
            method.instructions.insertBefore(armSwing, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "swingArm",
                    "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)V", false));
        }else{
            method.instructions.insert(attackEntity, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods",
                    "attackEntityClient", "(Lbsa;Laed;Lvg;)V", false));
            method.instructions.insertBefore(armSwing,
                    new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "swingArm", "(Laed;Lub;)V", false));
        }
        method.instructions.remove(attackEntity);
        method.instructions.remove(armSwing);
    }

    /** Add ModelRotationEvent in {@link net.minecraft.client.renderer.entity.RenderLivingBase#doRender} */
    /*@SuppressWarnings("unused")
    private static void patchRenderLivingBase(ClassNode clss, MethodNode method) {
    	asmDebug("Patching RenderLivingBase.doRender");
    	boolean optifine=false;
    	try {optifine = Class.forName("optifine.OptiFineClassTransformer")!=null;} catch (ClassNotFoundException e) {}
    	Iterator<AbstractInsnNode> it = (Iterator<AbstractInsnNode>)method.instructions.iterator();
    AbstractInsnNode node = null;
    AbstractInsnNode setRotationAngles = null;
    Method setRotationAnglesMethod = new Method("setRotationAngles", "func_78087_a", "a", "(FFFFFFLnet/minecraft/entity/Entity;)V", "(FFFFFFLvg;)V");
    while (it.hasNext()) {
    	node = it.next();
        if(node.getOpcode()==Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode)
        {                                 	
        	MethodInsnNode dyn = (MethodInsnNode) node;
        	//ModelBase.setRotationAngles
        	if(setRotationAnglesMethod.matches(dyn))
        		setRotationAngles=dyn;
        }                                 
    }
    //shouldSit local variable missing with optifine. this seems so easily breakable...
    int i = optifine?1:0;
    testNonNull(setRotationAngles, setRotationAnglesMethod);
    InsnList inject = new InsnList();
    inject.add(new VarInsnNode(Opcodes.FLOAD, 18-i));
    inject.add(new VarInsnNode(Opcodes.FLOAD, 17-i));
    inject.add(new VarInsnNode(Opcodes.FLOAD, 15-i));
    inject.add(new VarInsnNode(Opcodes.FLOAD, 13-i));
    inject.add(new VarInsnNode(Opcodes.FLOAD, 14-i));
    inject.add(new VarInsnNode(Opcodes.FLOAD, 16-i));
    inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
    if(ASMLoader.isDeobfEnvironment())
        inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "modelEvent", 
        		"(FFFFFFLnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/entity/RenderLivingBase;)V", false));
    else
    	inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "modelEvent", 
    			"(FFFFFFLvg;Lcaa;)V", false));
    method.instructions.insert(setRotationAngles, inject);
    }*/

    /** Add ModelPlayerRenderEvent in {@link net.minecraft.client.model.ModelPlayer#render} 
     * (Actually {@link net.minecraft.client.model.ModelBiped#render} ) */
    private static void patchModelPlayer(ClassNode clss, MethodNode method) {
        asmDebug("Patching ModelPlayer.doRender (Actually ModelBiped.doRender)");
        Iterator<AbstractInsnNode> it = method.instructions.iterator();
        AbstractInsnNode node = null;
        AbstractInsnNode setRotationAngles = null;
        Method setRotationAnglesMethod = new Method("setRotationAngles", "func_78087_a", "a", "(FFFFFFLnet/minecraft/entity/Entity;)V",
                "(FFFFFFLvg;)V");
        while(it.hasNext()){
            node = it.next();
            if(node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode){
                MethodInsnNode dyn = (MethodInsnNode) node;
                //ModelBase.setRotationAngles
                if(setRotationAnglesMethod.matches(dyn))
                    setRotationAngles = dyn;
            }
        }
        testNonNull(setRotationAngles, setRotationAnglesMethod);
        InsnList inject = new InsnList();
        inject.add(new VarInsnNode(Opcodes.FLOAD, 2));
        inject.add(new VarInsnNode(Opcodes.FLOAD, 3));
        inject.add(new VarInsnNode(Opcodes.FLOAD, 4));
        inject.add(new VarInsnNode(Opcodes.FLOAD, 5));
        inject.add(new VarInsnNode(Opcodes.FLOAD, 6));
        inject.add(new VarInsnNode(Opcodes.FLOAD, 7));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
        if(ASMLoader.isDeobfEnvironment())
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "modelPlayerEvent",
                    "(FFFFFFLnet/minecraft/entity/Entity;Lnet/minecraft/client/model/ModelBiped;)V", false));
        else
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "modelPlayerEvent", "(FFFFFFLvg;Lbpx;)V",
                    false));
        method.instructions.insert(setRotationAngles, inject);
    }

    /** Add PathFindInitEvent in {@link net.minecraft.pathfinding.PathNavigate#PathNavigate} */
    private static void patchPathNavigate(ClassNode clss, MethodNode method) {
        asmDebug("Patching PathNavigate");
        Iterator<AbstractInsnNode> it = method.instructions.iterator();
        AbstractInsnNode node = null;
        while(it.hasNext()){
            node = it.next();
            if(node.getOpcode() == Opcodes.RETURN){
                break;
            }
        }
        InsnList inject = new InsnList();
        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));

        if(ASMLoader.isDeobfEnvironment()){
            inject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/pathfinding/PathNavigate", "pathFinder",
                    "Lnet/minecraft/pathfinding/PathFinder;"));
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "pathFinderInitEvent",
                    "(Lnet/minecraft/pathfinding/PathNavigate;Lnet/minecraft/pathfinding/PathFinder;)Lnet/minecraft/pathfinding/PathFinder;", false));
            inject.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/pathfinding/PathNavigate", "pathFinder",
                    "Lnet/minecraft/pathfinding/PathFinder;"));
        }else{
            inject.add(new FieldInsnNode(Opcodes.GETFIELD, "ze", "r", "Lbem;"));
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "pathFinderInitEvent", "(Lze;Lbem;)Lbem;",
                    false));
            inject.add(new FieldInsnNode(Opcodes.PUTFIELD, "ze", "r", "Lbem;"));
        }
        method.instructions.insertBefore(node, inject);
    }

    /** Add LayerHeldItemEvent in {@link net.minecraft.client.renderer.entity.layers.LayerHeldItem#doRenderLayer} */
    private static void layerHeldItem(ClassNode clss, MethodNode method) {
        asmDebug("Patching LayerHeldItem.doRenderLayer");
        Iterator<AbstractInsnNode> it = method.instructions.iterator();
        AbstractInsnNode node = null;
        Method isEmpty = new Method("isEmpty", "func_78087_a", "a", "()Z", "()Z");

        while(it.hasNext()){
            node = it.next();
            if(node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode){
                MethodInsnNode m = (MethodInsnNode) node;
                if(isEmpty.matches(m)){
                    while(node.getPrevious().getOpcode() != Opcodes.ASTORE)
                        node = node.getPrevious();
                    break;
                }
            }
        }
        InsnList inject = new InsnList();
        //Right hand side
        /*inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 11));
        if(ASMLoader.isDeobfEnvironment())
        {
        		inject.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/util/EnumHand", "MAIN_HAND", "Lnet/minecraft/util/EnumHand;"));
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "layerHeldItemEvent", 
            		"(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;", false));
        		inject.add(new VarInsnNode(Opcodes.ASTORE, 11));
        }
        else
        {
        	inject.add(new FieldInsnNode(Opcodes.GETSTATIC, "ub", "a", "Lub;"));
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "layerHeldItemEvent", 
            		"(Lvp;Laip;Lub;)Laip;", false));
        		inject.add(new VarInsnNode(Opcodes.ASTORE, 11));
        }*/
        //Left hand side
        inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 10));
        if(ASMLoader.isDeobfEnvironment()){
            inject.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/util/EnumHand", "OFF_HAND", "Lnet/minecraft/util/EnumHand;"));
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "layerHeldItemEvent",
                    "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;",
                    false));
            inject.add(new VarInsnNode(Opcodes.ASTORE, 10));
        }else{
            inject.add(new FieldInsnNode(Opcodes.GETSTATIC, "ub", "b", "Lub;"));
            inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "layerHeldItemEvent",
                    "(Lvp;Laip;Lub;)Laip;", false));
            inject.add(new VarInsnNode(Opcodes.ASTORE, 10));
        }
        method.instructions.insertBefore(node, inject);
    }
}

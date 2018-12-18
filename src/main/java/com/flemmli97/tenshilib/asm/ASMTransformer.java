package com.flemmli97.tenshilib.asm;

import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.Maps;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer{

	private static Map<String, Transform> patches = Maps.newHashMap();
	private static Map<String, Method> classMethod = Maps.newHashMap();
	
	private interface Transform
	{
		public void apply(ClassNode clss, MethodNode method);
	}
	
	static
	{
		patches.put("net.minecraft.client.Minecraft", patchClickMouse());
		classMethod.put("net.minecraft.client.Minecraft", new Method("clickMouse", "func_147116_af", "aA", "()V", "()V"));
		patches.put("net.minecraft.client.renderer.entity.RenderLivingBase", patchRenderLivingBase());
		classMethod.put("net.minecraft.client.renderer.entity.RenderLivingBase", new Method("doRender", "func_76986_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", "(Lvp;DDDFF)V"));
		patches.put("net.minecraft.pathfinding.PathNavigate", patchPathNavigate());
		classMethod.put("net.minecraft.pathfinding.PathNavigate", new Method("<init>", "<init>", "<init>", "(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/world/World;)V", "(Lvq;Lamu;)V"));
		patches.put("net.minecraft.client.renderer.entity.layers.LayerHeldItem", layerHeldItem());
		classMethod.put("net.minecraft.client.renderer.entity.layers.LayerHeldItem", new Method("doRenderLayer", "func_177141_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFFF)V", "(Lvp;FFFFFFF)V"));
		//SHHH. There were various reasons why i did it this way and have not made a pull request for forge
		patches.put("net.minecraftforge.common.config.ConfigManager", configManager());
		classMethod.put("net.minecraftforge.common.config.ConfigManager", new Method("sync", "", "", 
				"(Lnet/minecraftforge/common/config/Configuration;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Object;)V", ""));
	}
	
	protected static void asmDebug(String debug)
	{
		System.out.println("[TenshiCore]: " + debug);
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ASMLoader.asmLoaded=true;
		if(patches.containsKey(transformedName))
		{
			return transform(basicClass, classMethod.get(transformedName), patches.get(transformedName));
		}
		return basicClass;
	}
		
	private static byte[] transform(byte[] clss, Method m, Transform transform)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(clss);
		classReader.accept(classNode, 0);
		try
		{
			for(MethodNode method : classNode.methods)
			{
				if(m.matches(method))
				{
					transform.apply(classNode, method);
					ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
					classNode.accept(writer);
					return writer.toByteArray();
				}
			}
			throw new ASMException("No Method found", m);
		}
		catch(ASMException e)
		{
			//A more visible alert, if some name changes and it cant find the method
			asmDebug("MethodNode not found for " + e.getMethod());
			e.printStackTrace();
		}
		return clss;
	}
	
	protected static void testNonNull(AbstractInsnNode node, Method m)
	{
		try
		{
			if(node==null)
				throw new ASMException("Instruction not found", m);
		}
		catch(ASMException e)
		{
			asmDebug("Instruction not found for " + e.getMethod());
			e.printStackTrace();
		}
	}
	
	private static Transform patchClickMouse()
	{
		return new Transform() {
			@Override
			public void apply(ClassNode clss, MethodNode method) {
				asmDebug("Patching Minecraft.clickMouse");
				Iterator<AbstractInsnNode> it = (Iterator<AbstractInsnNode>)method.instructions.iterator();
                AbstractInsnNode node = null;
                AbstractInsnNode attackEntity = null;
                AbstractInsnNode armSwing = null;
                Method attackEntityMethod = new Method("attackEntity", "func_78764_a", "a", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V", "(Laed;Lvg;)V");
                Method armSwingMethod = new Method("swingArm", "func_184609_a", "a", "(Lnet/minecraft/util/EnumHand;)V", "(Lub;)V");
                while (it.hasNext()) {
                    node = it.next();
                    if(node.getOpcode()==Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode)
                    {                                 	
                    	MethodInsnNode dyn = (MethodInsnNode) node;
                    	//playerController.attackEntity
                    	if(attackEntityMethod.matches(dyn))
                    		attackEntity=dyn;
                       	//player.swingArm
                    	if(armSwingMethod.matches(dyn))
                    	{
	                    	while(!(node.getPrevious() instanceof LineNumberNode))
	                    		node=node.getPrevious();	
	                    	armSwing=node.getPrevious();
                    	}
                    }                                 
                }
                testNonNull(attackEntity, attackEntityMethod);
                testNonNull(armSwing, armSwingMethod);
                method.instructions.insert(attackEntity,  new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "setSwing", 
    					"()V", false));
                method.instructions.insertBefore(armSwing, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "swingArm", 
    					"()V", false));
                method.instructions.insertBefore(armSwing, new InsnNode(Opcodes.RETURN));
			}			
		};
	}
	
	private static Transform patchRenderLivingBase()
	{
		return new Transform() {
			@Override
			public void apply(ClassNode clss, MethodNode method) {
				asmDebug("Patching RenderLivingBase.doRender");
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
                testNonNull(setRotationAngles, setRotationAnglesMethod);
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(Opcodes.FLOAD, 18));
                inject.add(new VarInsnNode(Opcodes.FLOAD, 17));
                inject.add(new VarInsnNode(Opcodes.FLOAD, 15));
                inject.add(new VarInsnNode(Opcodes.FLOAD, 13));
                inject.add(new VarInsnNode(Opcodes.FLOAD, 14));
                inject.add(new VarInsnNode(Opcodes.FLOAD, 16));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                if(ASMLoader.isDeobfEnvironment())
	                inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "modelEvent", 
	                		"(FFFFFFLnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/entity/RenderLivingBase;)V", false));
                else
                	inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "modelEvent", 
                			"(FFFFFFLvg;Lcaa;)V", false));
                method.instructions.insert(setRotationAngles, inject);
			}			
		};
	}
	
	private static Transform patchPathNavigate()
	{
		return new Transform() {
			@Override
			public void apply(ClassNode clss, MethodNode method) {
					asmDebug("Patching PathNavigate");
					Iterator<AbstractInsnNode> it = (Iterator<AbstractInsnNode>)method.instructions.iterator();
	                AbstractInsnNode node = null;
					while (it.hasNext()) {
	                	node = it.next();
	                    if(node.getOpcode()==Opcodes.RETURN)
	                    {                                 	
	                    	break;
	                    }                                 
	                }
					InsnList inject = new InsnList();
					inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					inject.add(new VarInsnNode(Opcodes.ALOAD, 0));

					if(ASMLoader.isDeobfEnvironment())
					{
						inject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/pathfinding/PathNavigate", "pathFinder", "Lnet/minecraft/pathfinding/PathFinder;"));
						inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "pathFinderInitEvent", 
								"(Lnet/minecraft/pathfinding/PathNavigate;Lnet/minecraft/pathfinding/PathFinder;)Lnet/minecraft/pathfinding/PathFinder;", false));
						inject.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/pathfinding/PathNavigate", "pathFinder", "Lnet/minecraft/pathfinding/PathFinder;"));
					}
					else
					{
						inject.add(new FieldInsnNode(Opcodes.GETFIELD, "ze", "r", "Lbem;"));
						inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "pathFinderInitEvent", 
								"(Lze;Lbem;)Lbem;", false));
						inject.add(new FieldInsnNode(Opcodes.PUTFIELD, "ze", "r", "Lbem;"));
					}
					method.instructions.insertBefore(node, inject);
			}};
	}
	
    
    private static Transform layerHeldItem() {
    	return new Transform() {
            @Override
            public void apply(ClassNode clss, MethodNode method) {
                asmDebug("Patching LayerHeldItem.doRenderLayer");
                Iterator<AbstractInsnNode> it = (Iterator<AbstractInsnNode>)method.instructions.iterator();
                AbstractInsnNode node = null;
                Method isEmpty = new Method("isEmpty", "func_78087_a", "a", "()Z", "()Z");

                while (it.hasNext()) 
                {
                    node = it.next();
                    if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode) 
                    {
                        MethodInsnNode m = (MethodInsnNode)node;
                        if(isEmpty.matches(m))
                        {
                        	while(node.getPrevious().getOpcode()!=Opcodes.ASTORE)
                        		node=node.getPrevious();
                        	break;
                        }
                    }
                }
                InsnList inject = new InsnList();
                //Right hand side
                inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
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
                }
                //Left hand side
                inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 10));
                if(ASMLoader.isDeobfEnvironment())
                {
    				inject.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/util/EnumHand", "OFF_HAND", "Lnet/minecraft/util/EnumHand;"));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "layerHeldItemEvent", 
                    		"(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;", false));
    				inject.add(new VarInsnNode(Opcodes.ASTORE, 10));
                }
                else
                {
                	inject.add(new FieldInsnNode(Opcodes.GETSTATIC, "ub", "b", "Lub;"));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "layerHeldItemEvent", 
                    		"(Lvp;Laip;Lub;)Laip;", false));
    				inject.add(new VarInsnNode(Opcodes.ASTORE, 10));
                }
                method.instructions.insertBefore(node, inject);
            }
        };
    }
    
    private static Transform configManager() {
    	return new Transform() {
			@Override
			public void apply(ClassNode clss, MethodNode method) {
				asmDebug("Patching ConfigManager.sync");
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
				inject.add(new VarInsnNode(Opcodes.ALOAD, 3));
				inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
				inject.add(new VarInsnNode(Opcodes.ALOAD, 5));
				inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/flemmli97/tenshilib/asm/ASMMethods", "configLoad", 
                		"(Lnet/minecraftforge/common/config/Configuration;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)V", false));
				method.instructions.insert(inject);
			}
        };
    }
}

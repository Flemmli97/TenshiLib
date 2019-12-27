package com.flemmli97.tenshilib.asm;

import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Method {

    protected final String name, srgName, obfName, desc, obfDesc;

    Method(String name, String srgName, String obfName, String desc, String obfDesc) {
        this.name = name;
        this.srgName = srgName;
        this.obfName = obfName;
        this.desc = desc;
        this.obfDesc = obfDesc;
    }

    public boolean matches(MethodNode method) {
        return (method.name.equals(this.name) || method.name.equals(this.srgName) || method.name.equals(this.obfName))
                && (method.desc.equals(this.desc) || method.desc.equals(this.obfDesc));
    }

    public boolean matches(MethodInsnNode method) {
        return (method.name.equals(this.name) || method.name.equals(this.srgName) || method.name.equals(this.obfName))
                && (method.desc.equals(this.desc) || method.desc.equals(this.obfDesc));
    }

    @Override
    public String toString() {
        return "Method: " + this.name;
    }
}

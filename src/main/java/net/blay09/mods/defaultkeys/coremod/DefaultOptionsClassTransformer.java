package net.blay09.mods.defaultkeys.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class DefaultOptionsClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.Minecraft")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, ClassReader.SKIP_DEBUG);
            for (MethodNode method : classNode.methods) {
                if ((method.name.equals("ag") || method.name.equals("startGame")) && method.desc.equals("()V")) {
                    method.instructions.insert(
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/blay09/mods/defaultkeys/DefaultKeys",
                            "preStartGame",
                            "()V",
                            false));
                    break;
                }
            }
            ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return basicClass;
    }

}

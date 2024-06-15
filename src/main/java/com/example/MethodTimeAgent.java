package com.example;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;

public class MethodTimeAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if ("com/example/YourTargetClass".replace('.', '/').equals(className)) {
                    return transformClass(classfileBuffer);
                }
                return classfileBuffer;
            }
        });
    }

    private static byte[] transformClass(byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new MethodTimeClassVisitor(Opcodes.ASM9, writer);
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    static class MethodTimeClassVisitor extends ClassVisitor {
        public MethodTimeClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (mv != null && !name.equals("<init>")) {
                mv = new MethodTimeMethodVisitor(api, mv);
            }
            return mv;
        }
    }

    static class MethodTimeMethodVisitor extends MethodVisitor {
        public MethodTimeMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/MethodTimeRecorder", "start", "()V", false);
        }

        @Override
        public void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/MethodTimeRecorder", "end", "()V", false);
            }
            super.visitInsn(opcode);
        }
    }
}

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
                if(className.startsWith("com"))
                    System.out.println(className);
                if(TargetList.targetList
                        .stream().map(TargetList.Target::getClassName)
                        .anyMatch(className::equals)) {
                    return transformClass(classfileBuffer, className);
                }
                return classfileBuffer;
            }
        });
    }

    private static byte[] transformClass(byte[] classfileBuffer, String className) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new MethodTimeClassVisitor(Opcodes.ASM9, writer, className);
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    static class MethodTimeClassVisitor extends ClassVisitor {
        private String className;
        public MethodTimeClassVisitor(int api, ClassVisitor classVisitor, String className) {
            super(api, classVisitor);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (mv != null
                    && !name.equals("<init>")
                    && TargetList.targetList
                    .stream().filter(t -> t.getClassName().equals(className))
                    .map(TargetList.Target::getMethodName)
                    .anyMatch(name::equals)){
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

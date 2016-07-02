package cz.neumimto.spongejs;


import jdk.internal.dynalink.beans.StaticClass;
import org.objectweb.asm.*;
import org.spongepowered.api.event.Event;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by NeumimTo on 12.10.15.
 */
public class ClassGenerator implements Opcodes {

    public static int it = 0;

    private static String toPath(Class cl) {
        return cl.getName().replaceAll("\\.", "/");
    }

    public static Object generateDynamicListener(Map<StaticClass, List<Consumer<? extends Event>>> map) {
        Object o = null;
        try {
            byte[] b = generateDynamicListenerbc(map);
            o = loadClass("cz.neumimto.spongejs.listeners.DynamicListener" + it, b);
            Class<?> listener = Class.forName("cz.neumimto.spongejs.listeners.DynamicListener" + it);
            o = listener.newInstance();
            System.out.println("Successfully loaded class " + listener.getClass().getName());
            for (Field field : listener.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    List s = (List) field.get(o);
                    ParameterizedType paramtype = (ParameterizedType) field.getGenericType();
                    ParameterizedType type = (ParameterizedType) paramtype.getActualTypeArguments()[0];
                    Class<? extends Event> event = (Class<? extends Event>) type.getActualTypeArguments()[0];
                    map.entrySet().stream()
                            .filter(m -> m.getKey().getRepresentedClass() == event)
                            .forEach(a -> s.addAll(a.getValue()));
                }
            }
            System.out.println("Successfully populated class " + listener.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }

    public static Class loadClass(String className, byte[] b) {
        Class clazz = null;
        try {
            ClassLoader loader = ScriptLoader.class.getClassLoader();
            Class cls = Class.forName("java.lang.ClassLoader");
            java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);

            method.setAccessible(true);
            try {
                Object[] args = new Object[]{className, b, 0, b.length};
                clazz = (Class) method.invoke(loader, args);
            } finally {
                method.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    private static byte[] generateDynamicListenerbc(Map<StaticClass, List<Consumer<? extends Event>>> set) throws Exception {
        System.out.println("Generating bytecode");
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "cz/neumimto/spongejs/listeners/DynamicListener" + it, null, "java/lang/Object", null);

        cw.visitSource("DynamicListener" + it + ".java", null);


        for (StaticClass e : set.keySet()) {
            String name = e.getRepresentedClass().getSimpleName().substring(0, 1).toLowerCase() + e.getRepresentedClass().getSimpleName().substring(1) + "s";
            fv = cw.visitField(ACC_PUBLIC, name, "Ljava/util/List;", "Ljava/util/List<Ljava/util/function/Consumer<L" + toPath(e.getRepresentedClass()) + ";>;>;", null);
            fv.visitEnd();
        }
        int i = 19;
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(17, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            for (StaticClass a : set.keySet()) {
                Class e = a.getRepresentedClass();
                String name = e.getSimpleName().substring(0, 1).toLowerCase() + e.getSimpleName().substring(1) + "s";
                System.out.println(String.format("Creating field \"%s\"", name));
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLineNumber(i, l1);
                i++;
                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(NEW, "java/util/ArrayList");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
                mv.visitFieldInsn(PUTFIELD, "cz/neumimto/spongejs/listeners/DynamicListener" + it, name, "Ljava/util/List;");
            }
            mv.visitInsn(RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", "Lcz/neumimto/spongejs/listeners/DynamicListener" + it + ";", null, l0, l3, 0);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
        }
        {
            for (StaticClass a : set.keySet()) {
                Class e = a.getRepresentedClass();
                String name = "on" + e.getSimpleName();
                System.out.println(String.format("Creating method \"%s\" ", name));
                String name1 = e.getSimpleName().substring(0, 1).toLowerCase() + e.getSimpleName().substring(1) + "s";
                mv = cw.visitMethod(ACC_PUBLIC, name, "(L" + toPath(e) + ";)V", null, null);
                {
                    av0 = mv.visitAnnotation("Lorg/spongepowered/api/event/Listener;", true);
                    av0.visitEnum("order", "Lorg/spongepowered/api/event/Order;", "BEFORE_POST");
                    av0.visitEnd();
                    mv.visitCode();
                    Label l0 = new Label();
                    mv.visitLabel(l0);
                    i += 3;
                    mv.visitLineNumber(i, l0);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "cz/neumimto/spongejs/listeners/DynamicListener" + it, name1, "Ljava/util/List;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
                    mv.visitVarInsn(ASTORE, 2);
                    Label l1 = new Label();
                    mv.visitLabel(l1);
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/util/Iterator"}, 0, null);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
                    Label l2 = new Label();
                    mv.visitJumpInsn(IFEQ, l2);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
                    mv.visitTypeInsn(CHECKCAST, "java/util/function/Consumer");
                    mv.visitVarInsn(ASTORE, 3);
                    Label l3 = new Label();
                    mv.visitLabel(l3);
                    i++;
                    mv.visitLineNumber(i, l3);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true);
                    Label l4 = new Label();
                    mv.visitLabel(l4);
                    i++;
                    mv.visitLineNumber(i, l4);
                    mv.visitJumpInsn(GOTO, l1);
                    mv.visitLabel(l2);
                    i++;
                    mv.visitLineNumber(i, l2);
                    mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
                    mv.visitInsn(RETURN);
                    Label l5 = new Label();
                    mv.visitLabel(l5);
                    mv.visitLocalVariable("it", "Ljava/util/function/Consumer;", "Ljava/util/function/Consumer<L" + toPath(e) + ";>;", l3, l4, 3);
                    mv.visitLocalVariable("this", "Lcz/neumimto/spongejs/listeners/DynamicListener" + it + ";", null, l0, l5, 0);
                    mv.visitLocalVariable("event", "L" + toPath(e) + ";", null, l0, l5, 1);
                    mv.visitMaxs(2, 4);
                    mv.visitEnd();
                }
            }
        }

        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        System.out.println("Generated " + bytes.length + " B");
        return bytes;
    }

}
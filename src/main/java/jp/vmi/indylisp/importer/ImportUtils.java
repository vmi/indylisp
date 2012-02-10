package jp.vmi.indylisp.importer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ImportUtils {

    private ImportUtils() {
    }

    public static String signature(Method method) {
        StringBuilder buffer = new StringBuilder();
        if (Modifier.isStatic(method.getModifiers()))
            buffer.append("static ");
        buffer.append(method.getReturnType().getSimpleName())
            .append(' ')
            .append(method.getName())
            .append('(');
        boolean comma = false;
        for (Class<?> ptype : method.getParameterTypes()) {
            if (comma)
                buffer.append(", ");
            else
                comma = true;
            if (ptype.isArray())
                buffer.append(ptype.getComponentType().getSimpleName()).append("[]");
            else
                buffer.append(ptype.getSimpleName());
        }
        buffer.append(')');
        return buffer.toString();
    }
}

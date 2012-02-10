package jp.vmi.indylisp.importer;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.vmi.indylisp.EvalException;
import jp.vmi.indylisp.annotations.Converter;
import jp.vmi.indylisp.objects.IndyObject;

import static jp.vmi.indylisp.importer.ImportUtils.*;

public class Converters {

    private static final Logger log = LoggerFactory.getLogger(Converters.class);

    private final Converters global;
    private final Map<Class<?>, MethodHandle> rvMap = new IdentityHashMap<>();
    private final Map<Class<?>, MethodHandle> pvMap = new IdentityHashMap<>();

    protected Converters(Converters global) {
        this.global = global;
    }

    public void putRvConv(Class<?> rtype, MethodHandle mh) {
        if (rvMap.containsKey(rtype))
            throw new EvalException("RvConverter for " + rtype + " is already registered.");
        rvMap.put(rtype, mh);
    }

    public void putPvConv(Class<?> ptype, MethodHandle mh) {
        if (pvMap.containsKey(ptype))
            throw new EvalException("PvConverter for " + ptype + " is already registered.");
        pvMap.put(ptype, mh);
    }

    public MethodHandle getRvConv(Class<?> rtype) {
        MethodHandle result = rvMap.get(rtype);
        if (result != null)
            return result;
        else if (global != null)
            return global.getRvConv(rtype);
        else
            return null;
    }

    public MethodHandle getPvConv(Class<?> ptype) {
        MethodHandle result = pvMap.get(ptype);
        if (result != null)
            return result;
        else if (global != null)
            return global.getPvConv(ptype);
        else
            return null;
    }

    public void registerConverters(Lookup lookup, Class<?> convClass) {
        registerConverters(lookup, convClass, Converter.class);
    }

    protected void registerConverters(Lookup lookup, Class<?> convClass, Class<? extends Annotation> anno) {
        if (log.isDebugEnabled()) {
            log.debug("Converter: {} ({})", convClass.getSimpleName(), convClass.getPackage());
            log.debug("  Annotation: @{}", anno.getSimpleName());
        }
        Method[] methods = convClass.getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(anno)) {
                log.debug("  Skip (no annotation): {}", signature(method));
                continue;
            }
            int mod = method.getModifiers();
            if (!Modifier.isPublic(mod) || !Modifier.isStatic(mod)) {
                log.warn("  Skip (not public static): {}", signature(method));
                continue;
            }
            Class<?> rtype = method.getReturnType();
            if (rtype == void.class || rtype == Void.class) {
                log.warn("  Skip (void or Void): {}", signature(method));
                continue;
            }
            Class<?>[] ptypes = method.getParameterTypes();
            if (ptypes.length != 1) {
                log.warn("  Skip (not single parameter): {}", signature(method));
                continue;
            }
            registerConverterMethod(lookup, method, rtype, ptypes[0]);
        }
        if (log.isDebugEnabled())
            log.debug("Converter: Done.");
    }

    protected MethodHandle getArraytoIndyObject() {
        return global.getArraytoIndyObject();
    }

    protected MethodHandle getIndyObjectsToArray() {
        return global.getIndyObjectsToArray();
    }

    private void registerConverterMethod(Lookup lookup, Method method, Class<?> rtype, Class<?> ptype) {
        try {
            if (IndyObject.class.isAssignableFrom(rtype)) {
                // Java object to IndyObject
                MethodHandle mh = lookup.unreflect(method);
                putRvConv(ptype, mh);
                Class<?> aptype = Array.newInstance(ptype, 0).getClass();
                MethodHandle amh = MethodHandles.insertArguments(getArraytoIndyObject(), 0, mh)
                    .asType(MethodType.methodType(IndyObject.class, aptype));
                putRvConv(aptype, amh);
                log.debug("  J->I: {}", signature(method));
            } else if (IndyObject.class.isAssignableFrom(ptype)) {
                // IndyObject to Java object
                MethodHandle mh = lookup.unreflect(method);
                putPvConv(rtype, mh);
                Class<?> artype = Array.newInstance(rtype, 0).getClass();
                MethodHandle amh = MethodHandles.insertArguments(getIndyObjectsToArray(), 0, mh, rtype)
                    .asType(MethodType.methodType(artype, IndyObject[].class))
                    .asVarargsCollector(IndyObject[].class);
                putPvConv(artype, amh);
                log.debug("  I->J: {}", signature(method));
            } else {
                log.debug("  Skip: {}", signature(method));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

package jp.vmi.indylisp.importer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.vmi.indylisp.Engine;
import jp.vmi.indylisp.annotations.Alias;
import jp.vmi.indylisp.annotations.Exclude;
import jp.vmi.indylisp.annotations.SpecialForm;
import jp.vmi.indylisp.bindings.Bindings;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.JavaMethod;
import jp.vmi.indylisp.objects.Symbol;

import static java.lang.invoke.MethodType.*;
import static jp.vmi.indylisp.bindings.BoundEntry.*;
import static jp.vmi.indylisp.importer.ImportUtils.*;
import static jp.vmi.indylisp.objects.Symbol.*;

public class Importer {

    private static final Logger log = LoggerFactory.getLogger(Importer.class);

    private static final String initImport = "initImport";
    private static final List<Class<?>[]> ALL = new ArrayList<>();

    private final Engine engine;
    private final Bindings bindings;
    private final Lookup lookup;
    private final Class<?> targetClass;
    private Symbol namespace = null;
    private Class<?> wrappedClass = null;
    private MethodHandle unwrapper = null;
    private final Converters converters;
    private final Map<String, List<Class<?>[]>> excludeMap = new HashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public static void importClass(Engine engine, Class<?> targetClass) {
        Importer importer = new Importer(engine, targetClass);
        importer.importClass();
    }

    private Importer(Engine engine, Class<?> targetClass) {
        this.engine = engine;
        bindings = engine.getCurrentBindings();
        converters = engine.newConverters();
        lookup = MethodHandles.publicLookup();
        this.targetClass = targetClass;
    }

    public void setNamespace(String namespace) {
        this.namespace = engine.symbol(namespace);
    }

    public void setWrappedClass(Class<?> wrappedClass) {
        this.wrappedClass = wrappedClass;
    }

    public void exclude(String name, Class<?>... ptypes) {
        List<Class<?>[]> list = excludeMap.get(name);
        if (list == null) {
            list = new ArrayList<>();
            excludeMap.put(name, list);
        }
        list.add(ptypes);
    }

    public void excludeAllOf(String name) {
        excludeMap.put(name, ALL);
    }

    private boolean isExcluded(Method method) {
        if (method.isAnnotationPresent(Exclude.class))
            return true;
        String name = method.getName();
        List<Class<?>[]> ptypesList = excludeMap.get(name);
        if (ptypesList == null)
            return false;
        else if (ptypesList == ALL)
            return true;
        Class<?>[] ptypes = method.getParameterTypes();
        for (Class<?>[] exclPTypes : ptypesList)
            if (Arrays.equals(ptypes, exclPTypes))
                return true;
        return false;
    }

    public void addAlias(String origin, String alias) {
        aliasMap.put(origin, alias);
    }

    private Symbol getMethodName(Method method, boolean isStatic) {
        Alias alias = method.getAnnotation(Alias.class);
        if (alias != null)
            return engine.symbol(alias.value());
        String origin = method.getName();
        if (isStatic)
            origin = namespace.getValue() + "." + origin;
        String name = aliasMap.get(origin);
        return engine.symbol(name != null ? name : origin);
    }

    private static int getFlags(Method method) {
        int flags = READ_ONLY;
        if (method.isVarArgs())
            flags |= VAR_ARGS;
        if (method.isAnnotationPresent(SpecialForm.class))
            flags |= SPECIAL_FORM;
        return flags;
    }

    public void importClass() {
        try {
            lookup.findStatic(targetClass, initImport, methodType(void.class, Importer.class)).invoke(this);
            if (wrappedClass != null)
                unwrapper = lookup.findVirtual(targetClass, "getValue", methodType(Object.class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (log.isDebugEnabled())
            log.debug("Import: {} / {}", targetClass.getSimpleName(),
                (wrappedClass != null) ? wrappedClass.getSimpleName() : "-");
        if (namespace == null)
            throw new RuntimeException("Need call setNamespace() in "
                + targetClass.getSimpleName() + "." + initImport + "()");
        converters.registerConverters(lookup, targetClass);
        // register symbol as class name on global binding.
        bindings.bind(namespace, NIL, 0, namespace, READ_ONLY);
        if (wrappedClass != null)
            importWrappedClass();
        else
            importTargetClass();
        if (log.isDebugEnabled())
            log.debug("Import: Done.");
    }

    private void importWrappedClass() {
        Method[] methods = wrappedClass.getMethods(); // all methods are public
        for (Method method : methods) {
            if (isExcluded(method)) {
                log.debug("  Skip: {}", signature(method));
                continue;
            }
            if (!registerWrapperMethod(method))
                registerWrappedMethod(method);
        }
    }

    private boolean registerWrapperMethod(Method method) {
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        int arity = method.getParameterTypes().length;
        MethodHandle mh;
        try {
            Class<?>[] wptypes = new Class<?>[arity];
            Arrays.fill(wptypes, IndyObject.class);
            if (method.isVarArgs())
                wptypes[arity - 1] = IndyObject[].class;
            // get wrapper method
            method = targetClass.getMethod(method.getName(), wptypes);
            mh = lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            return false;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Symbol name = getMethodName(method, isStatic);
        Symbol className = isStatic ? NIL : namespace;
        if (bindings.isReadOnly(name, className, arity))
            return true; // already registered.
        String signature = signature(method);
        bindings.bind(name, className, arity, new JavaMethod(signature, mh), getFlags(method));
        if (log.isDebugEnabled()) {
            log.debug("  WrapperMethod: {}", signature);
            log.debug("    Registered as {}.", name);
        }
        return true;
    }

    private void registerWrappedMethod(Method method) {
        try {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            String signature = signature(method);
            if (log.isDebugEnabled())
                log.debug("  WrappedMethod: {}", signature);
            // is convertable return type?
            Class<?> rtype = method.getReturnType();
            MethodHandle rvConv = converters.getRvConv(rtype);
            if (rvConv == null) {
                log.debug("    Can't convert return type {}", rtype.getSimpleName());
                return;
            }
            // is convertable parameter type?
            MethodHandle mh = lookup.unreflect(method);
            List<MethodHandle> pvConvList = new ArrayList<>();
            if (!isStatic) {
                Class<?> recvType = mh.type().parameterType(0);
                pvConvList.add(unwrapper.asType(methodType(recvType, targetClass)));
            }
            Class<?>[] ptypes = method.getParameterTypes();
            for (Class<?> ptype : ptypes) {
                MethodHandle pvConv = converters.getPvConv(ptype);
                if (pvConv == null) {
                    log.debug("    Can't convert parameter type {}", ptype.getSimpleName());
                    return;
                }
                pvConvList.add(pvConv);
            }
            // wrap method
            MethodHandle[] pvConvs = new MethodHandle[pvConvList.size()];
            pvConvs = pvConvList.toArray(pvConvs);
            mh = MethodHandles.filterReturnValue(mh, rvConv);
            mh = MethodHandles.filterArguments(mh, 0, pvConvs);
            if (method.isVarArgs())
                mh = mh.asVarargsCollector(IndyObject[].class);
            JavaMethod javaMethod = new JavaMethod(signature, mh);
            Symbol name = getMethodName(method, isStatic);
            bindings.bind(name, isStatic ? NIL : namespace, ptypes.length, javaMethod, getFlags(method));
            if (log.isDebugEnabled())
                log.debug("    Registered as {}.", name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void importTargetClass() {
        Method[] methods = targetClass.getMethods(); // all methods are public
        next: for (Method method : methods) {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            String signature = signature(method);
            if (isExcluded(method)) {
                log.debug("  Skip: {}", signature);
                continue next;
            }
            Class<?> rtype = method.getReturnType();
            if (!IndyObject.class.isAssignableFrom(rtype))
                continue next;
            Class<?>[] ptypes = method.getParameterTypes();
            int arity = ptypes.length;
            if (arity > 0) {
                Class<?> tail = ptypes[arity - 1];
                if (method.isVarArgs()
                    ? !IndyObject[].class.isAssignableFrom(tail)
                    : !IndyObject.class.isAssignableFrom(tail))
                    continue next;
                for (int i = 0; i <= arity - 2; i++)
                    if (!IndyObject.class.isAssignableFrom(ptypes[i]))
                        continue next;
            }
            try {
                MethodHandle mh = lookup.unreflect(method);
                Symbol className;
                if (mh.type().parameterType(0) == Engine.class) {
                    mh = mh.bindTo(engine);
                    if (method.isVarArgs())
                        mh = mh.asVarargsCollector(ptypes[ptypes.length - 1]);
                    className = NIL; // as function
                    arity = mh.type().parameterCount();
                } else {
                    className = isStatic ? NIL : namespace;
                }
                JavaMethod javaMethod = new JavaMethod(signature, mh);
                Symbol name = getMethodName(method, isStatic);
                bindings.bind(name, className, arity, javaMethod, getFlags(method));
                if (log.isDebugEnabled()) {
                    log.debug("  IndyMethod: {}", signature);
                    log.debug("    Registered as {}.", name);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

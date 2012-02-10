package jp.vmi.indylisp.importer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;

import jp.vmi.indylisp.EvalException;
import jp.vmi.indylisp.annotations.GlobalConverter;
import jp.vmi.indylisp.objects.CellList;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.NumberWrapper;
import jp.vmi.indylisp.objects.StringWrapper;
import jp.vmi.indylisp.objects.Wrapper;

import static java.lang.invoke.MethodType.*;
import static jp.vmi.indylisp.objects.Symbol.*;

public final class GlobalConverters extends Converters {

    final MethodHandle arraytoIndyObject;
    final MethodHandle indyObjectsToArray;

    public GlobalConverters() {
        super(null);
        Lookup lookup = MethodHandles.publicLookup();
        Class<?> thisClass = getClass();
        try {
            arraytoIndyObject = lookup.findStatic(thisClass, "arrayToIndyObject",
                methodType(IndyObject.class, MethodHandle.class, Object[].class));
            indyObjectsToArray = lookup.findStatic(thisClass, "indyObjectsToArray",
                methodType(Object[].class, MethodHandle.class, Class.class, IndyObject[].class));
            MethodHandle voidToIndyObject = lookup.findStatic(thisClass, "voidToIndyObject",
                methodType(IndyObject.class));
            putRvConv(void.class, voidToIndyObject);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        registerConverters(lookup, thisClass, GlobalConverter.class);
        registerConverters(lookup, StringWrapper.class, GlobalConverter.class);
        registerConverters(lookup, NumberWrapper.class, GlobalConverter.class);
    }

    public Converters newConverters() {
        return new Converters(this);
    }

    @Override
    protected MethodHandle getArraytoIndyObject() {
        return arraytoIndyObject;
    }

    @Override
    protected MethodHandle getIndyObjectsToArray() {
        return indyObjectsToArray;
    }

    public static IndyObject arrayToIndyObject(MethodHandle rvConv, Object[] array) {
        CellList result = new CellList();
        try {
            for (Object elem : array)
                result.append((IndyObject) rvConv.invoke(elem));
        } catch (Throwable t) {
            throw new EvalException(t);
        }
        return result.toCell();
    }

    public static Object[] indyObjectsToArray(MethodHandle pvConv, Class<?> ctype, IndyObject... objs) {
        try {
            Object[] array = (Object[]) Array.newInstance(ctype, objs.length);
            for (int i = 0; i < objs.length; i++)
                array[i] = pvConv.invoke(objs[i]);
            return array;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static IndyObject voidToIndyObject() {
        return T;
    }

    @GlobalConverter
    public static IndyObject booleanToIndyObject(boolean value) {
        return value ? T : NIL;
    }

    @GlobalConverter
    public static boolean indyObjectToBoolean(IndyObject value) {
        return !value.isNil();
    }

    @GlobalConverter
    public static Object indyObjectToObject(IndyObject value) {
        return (value instanceof Wrapper<?>) ? value.getValue() : value;
    }
}

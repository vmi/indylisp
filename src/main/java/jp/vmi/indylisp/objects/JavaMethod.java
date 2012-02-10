package jp.vmi.indylisp.objects;

import java.lang.invoke.MethodHandle;

import jp.vmi.indylisp.EvalException;

public class JavaMethod extends IndyObject implements Method {

    public static final String NAMESPACE = JavaMethod.class.getSimpleName();

    private final String signature;
    private final MethodHandle methodHandle;

    public JavaMethod(String signature, MethodHandle methodHandle) {
        this.signature = signature;
        this.methodHandle = methodHandle;
    }

    @Override
    public IndyObject invoke(IndyObject... args) {
        try {
            return (IndyObject) methodHandle.invokeWithArguments((Object[]) args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new EvalException(e);
        }
    }

    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toExprString() {
        return toString();
    }

    @Override
    public String toString() {
        return "[" + signature + "]";
    }
}

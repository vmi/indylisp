package jp.vmi.indylisp.objects;

import java.util.Iterator;

import jp.vmi.indylisp.bindings.Bindings;

import static jp.vmi.indylisp.objects.Symbol.*;

public class Lambda extends IndyObject {

    public static final String NAMESPACE = Lambda.class.getSimpleName();

    private final Bindings parentBindings;
    private final IndyObject argNameList;
    private final IndyObject[] exprList;

    public Lambda(Bindings parentBindings, IndyObject argNameList, IndyObject[] exprList) {
        this.parentBindings = parentBindings;
        this.argNameList = argNameList;
        this.exprList = exprList;
    }

    public Bindings assignParameters(IndyObject[] argList) {
        Bindings newBindings = new Bindings(parentBindings);
        Iterator<IndyObject> iter = argNameList.iterator();
        for (IndyObject arg : argList) {
            if (!iter.hasNext())
                break;
            newBindings.bind((Symbol) iter.next(), NIL, 0, arg, 0);
        }
        while (iter.hasNext())
            newBindings.bind((Symbol) iter.next(), NIL, 0, NIL, 0);
        return newBindings;
    }

    public IndyObject[] getExprList() {
        return exprList;
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
    public String toString() {
        return toExprString();
    }

    @Override
    public String toExprString() {
        StringBuilder buffer = new StringBuilder("(lambda ");
        argNameListToExprString(buffer, argNameList);
        buffer.append("\n");
        exprListToExprString(buffer, exprList);
        return buffer.append(")\n").toString();
    }

    public static void argNameListToExprString(StringBuilder buffer, IndyObject argNameList) {
        buffer.append("(");
        boolean needSep = false;
        for (IndyObject argName : argNameList) {
            if (needSep)
                buffer.append(" ");
            else
                needSep = true;
            buffer.append(argName.toString());
        }
        buffer.append(")");
    }

    public static void exprListToExprString(StringBuilder buffer, IndyObject[] exprList) {
        for (IndyObject expr : exprList)
            buffer.append("  ").append(expr.toExprString()).append("\n");
    }
}

package jp.vmi.indylisp.objects;

public class LazyExprList extends IndyObject {

    private final IndyObject[] exprList;

    public LazyExprList(IndyObject... exprList) {
        this.exprList = exprList;
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
        return null;
    }

    @Override
    public String toString() {
        return toExprString();
    }

    @Override
    public String toExprString() {
        StringBuilder buffer = new StringBuilder(getClass().getSimpleName()).append("(\n");
        Lambda.exprListToExprString(buffer, exprList);
        return buffer.append(")\n").toString();
    }
}

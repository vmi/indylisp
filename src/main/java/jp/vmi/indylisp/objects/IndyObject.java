package jp.vmi.indylisp.objects;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import jp.vmi.indylisp.EvalException;

public abstract class IndyObject implements Iterable<IndyObject> {

    //
    // Common methods
    //

    abstract public boolean isAtom();

    public boolean isSymbol() {
        return false;
    }

    public boolean isNil() {
        return false;
    }

    abstract public String getNamespace();

    public IndyObject car() {
        throw new EvalException("this is not cons cell.");
    }

    public IndyObject cdr() {
        throw new EvalException("this is not cons cell.");
    }

    public <T extends IndyObject> T setcar(T car) {
        throw new EvalException("this is not cons cell.");
    }

    public <T extends IndyObject> T setcdr(T cdr) {
        throw new EvalException("this is not cons cell.");
    }

    public Object getValue() {
        throw new EvalException("this is not atom.");
    }

    public IndyObject[] toArray() {
        throw new EvalException("this is not cons cell.");
    }

    public abstract String toExprString();

    //
    // Iterator
    //

    public static class IndyObjectIterator implements Iterator<IndyObject> {

        private IndyObject obj;

        private IndyObjectIterator(IndyObject obj) {
            this.obj = obj;
        }

        @Override
        public boolean hasNext() {
            return !obj.isNil();
        }

        @Override
        public IndyObject next() {
            IndyObject result = obj.car();
            obj = obj.cdr();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove method is not supported.");
        }
    }

    @Override
    public Iterator<IndyObject> iterator() {
        return new IndyObjectIterator(this);
    }

    //
    // IndyObject to String
    //

    private static ThreadLocal<Map<Cell, Boolean>> scannedCellMap = new ThreadLocal<>();

    public static String toExprString(IndyObject obj) {
        if (obj.isAtom())
            return obj.toExprString();
        Cell cell = (Cell) obj;
        boolean needRemove = false;
        Map<Cell, Boolean> map = scannedCellMap.get();
        if (map == null) {
            map = new IdentityHashMap<>();
            needRemove = true;
            scannedCellMap.set(map);
        } else if (map.containsKey(obj))
            return "...";
        try {
            StringBuilder buf = new StringBuilder("(");
            while (true) {
                map.put(cell, Boolean.TRUE);
                IndyObject car = cell.car();
                if (car == null)
                    buf.append("[null]");
                else if (car == cell)
                    buf.append("[self]");
                else
                    buf.append(car.toExprString());
                IndyObject cdr = cell.cdr();
                if (cdr.isNil())
                    break;
                else if (cdr.isAtom()) {
                    buf.append(" . ").append(cdr.toExprString());
                    break;
                } else if (map.get(cdr) != null) {
                    buf.append(" ...");
                    break;
                }
                buf.append(' ');
                cell = (Cell) cdr;
            }
            buf.append(')');
            return buf.toString();
        } finally {
            if (needRemove)
                scannedCellMap.remove();
        }
    }
}

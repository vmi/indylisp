package jp.vmi.indylisp.objects;

import java.util.HashMap;

public class Symbol extends IndyObject {

    public static class SymbolTable extends HashMap<String, Symbol> {

        private static final long serialVersionUID = 1655796480738610490L;

        public SymbolTable() {
            super();
            put(NIL.getValue(), NIL);
            put(T.getValue(), T);
        }

        public Symbol symbol(String name) {
            Symbol symbol = get(name);
            if (symbol == null)
                put(name, symbol = new Symbol(name));
            return symbol;
        }
    }

    public static final Symbol NIL = new Nil();
    public static final Symbol T = new Symbol("t");

    private final String value;

    protected Symbol(String value) {
        this.value = value;
    }

    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public boolean isSymbol() {
        return true;
    }

    @Override
    public String getNamespace() {
        return value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toExprString() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

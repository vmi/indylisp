package jp.vmi.indylisp.bindings;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import jp.vmi.indylisp.EvalException;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.Symbol;

import static jp.vmi.indylisp.bindings.BoundEntry.*;
import static jp.vmi.indylisp.objects.Symbol.*;

public class Bindings {

    private static class BindingMap extends IdentityHashMap<Symbol, BoundEntryList> {

        private static final long serialVersionUID = 7424234288480959898L;

        @Override
        public BoundEntryList get(Object key) {
            BoundEntryList value = super.get(key);
            return value != null ? value : EMPTY_LIST;
        }
    }

    private static class BoundEntryList extends ArrayList<BoundEntry> {

        private static final long serialVersionUID = -2495819279714766796L;

        public BindingMap namespacedMap = EMPTY_MAP;
        public boolean hasVarArgs = false;

        @Override
        public BoundEntry get(int index) {
            return (index < size()) ? super.get(index) : NOT_FOUND;
        }

        @Override
        public BoundEntry set(int index, BoundEntry entry) {
            int size = size();
            if (index < size)
                return super.set(index, entry);
            while (size++ < index)
                add(NOT_FOUND);
            add(entry);
            return NOT_FOUND;
        }
    }

    private static final BoundEntry NOT_FOUND = new BoundEntry(NIL, 0);

    private static final BindingMap EMPTY_MAP = new BindingMap() {

        private static final long serialVersionUID = -3318238367668191888L;

        @Override
        public BoundEntryList get(Object key) {
            return EMPTY_LIST;
        }

        @Override
        public BoundEntryList put(Symbol key, BoundEntryList value) {
            throw new EvalException("EMPTY_MAP is not modifiable.");
        }
    };

    // "new BoundEntryList()" requires instanciated EMPTY_MAP.
    private static final BoundEntryList EMPTY_LIST = new BoundEntryList() {

        private static final long serialVersionUID = 3867360322809458687L;

        @Override
        public BoundEntry get(int index) {
            return NOT_FOUND;
        }

        @Override
        public BoundEntry set(int index, BoundEntry entry) {
            throw new EvalException("EMPTY_LIST is not modifiable.");
        }
    };

    public static boolean exists(BoundEntry entry) {
        return entry != NOT_FOUND;
    }

    private final Bindings parent;
    private final BindingMap map = new BindingMap();

    public Bindings(Bindings parent) {
        this.parent = parent;
        BoundEntryList nsEntry = new BoundEntryList();
        nsEntry.namespacedMap = map;
        map.put(NIL, nsEntry);
        bind(NIL, NIL, 0, NIL, READ_ONLY);
        bind(T, NIL, 0, T, READ_ONLY);
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public IndyObject bind(Symbol name, Symbol namespace, int arity, IndyObject value, int flags) {
        BoundEntryList nsEntry = map.get(namespace);
        if (nsEntry == EMPTY_LIST)
            map.put(namespace, nsEntry = new BoundEntryList());
        BindingMap lookupMap = nsEntry.namespacedMap;
        if (lookupMap == EMPTY_MAP)
            nsEntry.namespacedMap = lookupMap = new BindingMap();
        BoundEntryList entries = lookupMap.get(name);
        if (entries == EMPTY_LIST)
            lookupMap.put(name, entries = new BoundEntryList());
        BoundEntry entry = entries.get(arity);
        if (entry.isReadOnly())
            throw new EvalException(
                String.format("%s:%s:%d is read only. current value: %s / new value: %s",
                    namespace, name, arity, entry.getValue(), value));
        entry = new BoundEntry(value, flags);
        entries.set(arity, entry);
        if (entry.isVarArgs())
            entries.hasVarArgs = true;
        return value;
    }

    private BoundEntry lookupInternal(Symbol name, Symbol namespace, int arity) {
        BoundEntryList entries = map.get(namespace).namespacedMap.get(name);
        BoundEntry entry = entries.get(arity);
        if (entry != NOT_FOUND || !entries.hasVarArgs)
            return entry;
        for (int i = Math.min(arity - 1, entries.size() - 1); i >= 0; --i) {
            entry = entries.get(i);
            if (entry.isVarArgs())
                return entry;
        }
        return NOT_FOUND;
    }

    public BoundEntry lookup(Symbol name, Symbol namespace, int arity) {
        Bindings current = this;
        do {
            BoundEntry entry = current.lookupInternal(name, namespace, arity);
            if (entry != NOT_FOUND)
                return entry;
        } while ((current = current.parent) != null);
        return NOT_FOUND;
    }

    public boolean isReadOnly(Symbol name, Symbol namespace, int arity) {
        return map.get(namespace).namespacedMap.get(name).get(arity).isReadOnly();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}

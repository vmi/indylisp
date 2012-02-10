package jp.vmi.indylisp.objects;

import jp.vmi.indylisp.importer.Importer;

public class Nil extends Symbol {

    private static String NAMESPACE = "Nil";

    private static final IndyObject[] EMPTY_ARRAY = new IndyObject[0];

    Nil() {
        super("nil");
    }

    @Override
    public boolean isNil() {
        return true;
    }

    @Override
    public IndyObject car() {
        return this;
    }

    @Override
    public IndyObject cdr() {
        return this;
    }

    @Override
    public IndyObject[] toArray() {
        return EMPTY_ARRAY;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
    }
}

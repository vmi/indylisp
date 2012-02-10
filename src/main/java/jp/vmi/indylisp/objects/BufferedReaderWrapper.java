package jp.vmi.indylisp.objects;

import java.io.BufferedReader;

import jp.vmi.indylisp.importer.Importer;

public class BufferedReaderWrapper extends Wrapper<BufferedReader> {

    public static final String NAMESPACE = "BufferedReader";

    public BufferedReaderWrapper(BufferedReader value) {
        super(value);
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toExprString() {
        return "[" + NAMESPACE + "/" + getValue() + "]";
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
        importer.setWrappedClass(BufferedReader.class);
    }
}

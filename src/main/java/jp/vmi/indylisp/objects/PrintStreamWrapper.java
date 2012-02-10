package jp.vmi.indylisp.objects;

import java.io.PrintStream;
import java.util.Locale;

import jp.vmi.indylisp.annotations.Converter;
import jp.vmi.indylisp.importer.Importer;

import static jp.vmi.indylisp.objects.Symbol.*;

public class PrintStreamWrapper extends Wrapper<PrintStream> {

    public static final String NAMESPACE = "PrintStream";

    public PrintStreamWrapper(PrintStream value) {
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

    @Converter
    public static IndyObject ignorePrintStream(PrintStream ps) {
        return T;
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
        importer.setWrappedClass(PrintStream.class);
        importer.exclude("print", boolean.class);
        importer.exclude("print", char.class);
        importer.exclude("print", int.class);
        importer.exclude("print", long.class);
        importer.exclude("print", float.class);
        importer.exclude("print", double.class);
        importer.exclude("print", char[].class);
        importer.exclude("print", String.class);
        importer.exclude("println", boolean.class);
        importer.exclude("println", char.class);
        importer.exclude("println", int.class);
        importer.exclude("println", long.class);
        importer.exclude("println", float.class);
        importer.exclude("println", double.class);
        importer.exclude("println", char[].class);
        importer.exclude("println", String.class);
        importer.exclude("printf", Locale.class, String.class, Object[].class);
    }
}

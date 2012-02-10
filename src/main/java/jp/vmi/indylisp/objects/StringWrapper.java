package jp.vmi.indylisp.objects;

import jp.vmi.indylisp.EvalException;
import jp.vmi.indylisp.annotations.GlobalConverter;
import jp.vmi.indylisp.importer.Importer;

public class StringWrapper extends Wrapper<String> {

    public static final String NAMESPACE = "String";

    private StringWrapper(String value) {
        super(value);
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toExprString() {
        return "\"" + getValue().replaceAll("\"", "\\\"") + "\"";
    }

    @GlobalConverter
    public static IndyObject getInstance(String value) {
        return new StringWrapper(value);
    }

    @GlobalConverter
    public static IndyObject getInstance(CharSequence value) {
        return new StringWrapper(value.toString());
    }

    @GlobalConverter
    public static String toString(IndyObject obj) {
        if (obj instanceof StringWrapper)
            return ((StringWrapper) obj).getValue();
        throw new EvalException("Not string: " + obj);
    }

    @GlobalConverter
    public static CharSequence toCharSequence(IndyObject obj) {
        return toString(obj);
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
        importer.setWrappedClass(String.class);
        importer.addAlias("String.format", "format");
        importer.exclude("indexOf", int.class);
        importer.exclude("indexOf", int.class, int.class);
        importer.exclude("lastIndexOf", int.class);
        importer.exclude("lastIndexOf", int.class, int.class);
        importer.exclude("compareTo", Object.class);
        importer.excludeAllOf("valueOf");
    }
}

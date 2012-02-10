package jp.vmi.indylisp.bindings;

import jp.vmi.indylisp.objects.IndyObject;

public class BoundEntry {

    public static final int READ_ONLY = 0b0000_0001;
    public static final int VAR_ARGS = 0b0000_0010;
    public static final int SPECIAL_FORM = 0b0000_0100;

    private final IndyObject value;
    private final boolean isReadOnly;
    private final boolean isVarArgs;
    private final boolean isSpecialForm;

    private static boolean isTrue(int value, int flags) {
        return (value & flags) != 0;
    }

    BoundEntry(IndyObject value, int type) {
        this.value = value;
        this.isReadOnly = isTrue(type, READ_ONLY);
        this.isVarArgs = isTrue(type, VAR_ARGS);
        this.isSpecialForm = isTrue(type, SPECIAL_FORM);
    }

    public IndyObject getValue() {
        return value;
    }

    public boolean isSpecialForm() {
        return isSpecialForm;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("[").append(value.toString());
        if (isSpecialForm)
            buffer.append(":SF");
        if (isVarArgs)
            buffer.append(":VA");
        if (isReadOnly)
            buffer.append(":RO");
        return buffer.append("]").toString();
    }
}

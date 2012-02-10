package jp.vmi.indylisp.objects;

public abstract class Wrapper<T> extends IndyObject {

    private final T value;

    public Wrapper(T value) {
        this.value = value;
    }

    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        else if (that == null || this.getClass() != that.getClass())
            return false;
        return value.equals(((Wrapper<?>) that).value);
    }
}

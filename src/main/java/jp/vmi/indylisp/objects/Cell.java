package jp.vmi.indylisp.objects;

import java.util.ArrayList;
import java.util.List;

import jp.vmi.indylisp.importer.Importer;

import static jp.vmi.indylisp.objects.Symbol.*;

public class Cell extends IndyObject {

    public static final String NAMESPACE = "Cell";

    protected IndyObject car;
    protected IndyObject cdr;

    public Cell(IndyObject car, IndyObject cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public Cell(IndyObject car) {
        this(car, NIL);
    }

    public Cell() {
        this(null);
    }

    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public IndyObject car() {
        return car;
    }

    @Override
    public IndyObject cdr() {
        return cdr;
    }

    @Override
    public <T extends IndyObject> T setcar(T car) {
        this.car = car;
        return car;
    }

    @Override
    public <T extends IndyObject> T setcdr(T cdr) {
        this.cdr = cdr;
        return cdr;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toExprString() {
        return toExprString(this);
    }

    @Override
    public String toString() {
        return toExprString(this);
    }

    @Override
    public IndyObject[] toArray() {
        List<IndyObject> list = new ArrayList<>();
        for (IndyObject car : this)
            list.add(car);
        return list.toArray(new IndyObject[list.size()]);
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
    }
}

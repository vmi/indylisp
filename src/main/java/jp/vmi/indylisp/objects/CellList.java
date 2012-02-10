package jp.vmi.indylisp.objects;

import static jp.vmi.indylisp.objects.Symbol.*;

public class CellList extends Cell {

    public CellList() {
        car = this;
        cdr = NIL;
    }

    public CellList append(IndyObject... objs) {
        for (IndyObject obj : objs)
            car = car.setcdr(new Cell(obj));
        return this;
    }

    public IndyObject toCell() {
        return cdr;
    }

    @Override
    public String toExprString() {
        return getClass().getSimpleName() + "[" + cdr.toExprString() + "]";
    }

    @Override
    public String toString() {
        return cdr.toString();
    }
}

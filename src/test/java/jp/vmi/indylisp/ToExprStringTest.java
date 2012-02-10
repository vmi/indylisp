package jp.vmi.indylisp;

import java.math.BigDecimal;

import org.junit.Test;

import jp.vmi.indylisp.objects.Cell;
import jp.vmi.indylisp.objects.CellList;
import jp.vmi.indylisp.objects.NumberWrapper;
import jp.vmi.indylisp.objects.StringWrapper;
import jp.vmi.indylisp.objects.Symbol.SymbolTable;

import static jp.vmi.indylisp.objects.Symbol.*;
import static org.junit.Assert.*;

public class ToExprStringTest {

    @Test
    public void testToExprString() {
        SymbolTable symbolTable = new SymbolTable();
        CellList cellList = new CellList();
        assertEquals("CellList[nil]", cellList.toExprString());
        Cell cell;
        cell = new Cell();
        assertEquals("([null])", cell.toExprString());
        cell.setcar(cell);
        cell.setcdr(cell);
        assertEquals("([self] ...)", cell.toExprString());
        cell.setcar(NumberWrapper.getInstance(new BigDecimal("1.0")));
        cell.setcdr(NIL);
        assertEquals("(1.0)", cell.toExprString());
        cell.setcar(StringWrapper.getInstance("test"));
        assertEquals("(\"test\")", cell.toExprString());
        cell.setcar(symbolTable.symbol("test"));
        assertEquals("(test)", cell.toExprString());
        cell = new Cell(cell);
        assertEquals("((test))", cell.toExprString());
        cell.setcdr(symbolTable.symbol("test2"));
        assertEquals("((test) . test2)", cell.toExprString());
        cell.setcdr(new Cell(cell.cdr()));
        assertEquals("((test) test2)", cell.toExprString());
        cell.cdr().setcdr(cell);
        assertEquals("((test) test2 ...)", cell.toExprString());
    }
}

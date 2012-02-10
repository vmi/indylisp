package jp.vmi.indylisp;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import jp.vmi.indylisp.objects.Cell;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.Symbol;
import jp.vmi.indylisp.objects.Symbol.SymbolTable;

import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testParse() throws IOException {
        String str = "(define hello-world (text) (cond (text (print \"hello\" text)) (t (print \"hello world\" 10 'qsymbol))))";
        StringReader r = new StringReader(str);
        TokenReader tr = new TokenReader(r);
        Parser p = new Parser(tr, new SymbolTable());
        IndyObject head = p.getExprList();
        assertEquals(Cell.class, head.getClass());
        IndyObject obj = head.car();
        assertEquals(Cell.class, obj.getClass());
        assertEquals(Symbol.class, obj.car().getClass());
        assertEquals("define", obj.car().getValue());
        obj = obj.cdr();
        assertEquals(Symbol.class, obj.car().getClass());
        assertEquals("hello-world", obj.car().getValue());
        obj = obj.cdr();
        assertEquals(Cell.class, obj.car().getClass());
        assertEquals(Symbol.class, obj.car().car().getClass());
        assertEquals("text", obj.car().car().getValue());
        assertEquals(Symbol.NIL, obj.car().cdr());
        assertEquals(str.replaceAll("'([^\\s()\"']+)", "(quote $1)"), head.car().toExprString());
    }
}

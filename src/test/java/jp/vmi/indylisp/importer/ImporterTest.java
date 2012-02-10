package jp.vmi.indylisp.importer;

import java.math.BigDecimal;

import org.junit.Test;

import jp.vmi.indylisp.Engine;
import jp.vmi.indylisp.bindings.BoundEntry;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.JavaMethod;
import jp.vmi.indylisp.objects.NumberWrapper;
import jp.vmi.indylisp.objects.StringWrapper;

import static jp.vmi.indylisp.objects.Symbol.*;
import static org.junit.Assert.*;

public class ImporterTest {

    @Test
    public void testImportMethods() {
        Engine engine = Engine.newInstance();
        NumberWrapper a = NumberWrapper.getInstance(1);
        NumberWrapper b = NumberWrapper.getInstance(10);
        BoundEntry entry = engine.getCurrentBindings().lookup(engine.symbol("divide"),
            engine.getSymbolTable().get(NumberWrapper.NAMESPACE), 1);
        assertTrue(entry.isReadOnly());
        IndyObject value = entry.getValue();
        assertTrue(value instanceof JavaMethod);
        IndyObject c = ((JavaMethod) value).invoke(a, b);
        assertTrue(c instanceof NumberWrapper);
        assertEquals(new BigDecimal("0.1"), c.getValue());

        entry = engine.getCurrentBindings().lookup(engine.symbol("format"), NIL, 2);
        value = entry.getValue();
        StringWrapper sa = (StringWrapper) StringWrapper.getInstance("[%s]");
        StringWrapper sb = (StringWrapper) StringWrapper.getInstance("test");
        IndyObject sc = ((JavaMethod) value).invoke(sa, sb);
        assertTrue(sa instanceof StringWrapper);
        assertEquals("[test]", sc.getValue());
    }
}

package jp.vmi.indylisp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.Test;

import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.PrintStreamWrapper;

import static jp.vmi.indylisp.objects.Symbol.*;
import static org.junit.Assert.*;

public class FuncTest {

    private static final String FUNCTEST = "functest";
    private static final String FUNCTEST_IL = FUNCTEST + ".il";
    private static final String FUNCTEST_RESULT_TXT = FUNCTEST + "-result.txt";

    @Test
    public void testEval() throws Exception {
        Class<?> thisClass = getClass();

        // Test parser
        BufferedReader parsedReader = new BufferedReader(new InputStreamReader(
            thisClass.getResourceAsStream(FUNCTEST_IL)));
        StringBuilder expected = new StringBuilder("(");
        String line;
        while ((line = parsedReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0 && !line.matches("\\w*;.*")) {
                if (expected.charAt(expected.length() - 1) != '(' && line.charAt(0) != ')')
                    expected.append(" ");
                expected.append(line);
            }
        }
        expected.append(")");
        parsedReader.close();
        InputStream is = thisClass.getResourceAsStream(FUNCTEST_IL);
        TokenReader tr = new TokenReader(new InputStreamReader(is, "UTF-8"));
        Engine engine = Engine.newInstance();
        Parser p = new Parser(tr, engine.getSymbolTable());
        IndyObject result = p.getExprList();
        assertEquals(expected.toString(), result.toExprString());

        // Test engine
        InputStreamReader resultReader = new InputStreamReader(
            thisClass.getResourceAsStream(FUNCTEST_RESULT_TXT));
        expected = new StringBuilder();
        int c;
        while ((c = resultReader.read()) != -1)
            expected.append((char) c);
        resultReader.close();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        PrintStreamWrapper out = new PrintStreamWrapper(ps);
        engine.getCurrentBindings().bind(engine.symbol("out"), NIL, 0, out, 0);
        engine.progn(result.toArray());
        ps.close();
        assertEquals(expected.toString(), os.toString("UTF-8").replaceAll("\r\n", "\n"));
    }
}

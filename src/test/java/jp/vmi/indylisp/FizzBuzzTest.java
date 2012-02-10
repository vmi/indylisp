package jp.vmi.indylisp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.Test;

import jp.vmi.indylisp.TokenReader.Token;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.PrintStreamWrapper;

import static jp.vmi.indylisp.objects.Symbol.*;
import static org.junit.Assert.*;

public class FizzBuzzTest {

    @Test
    public void testEval() throws Exception {
        Class<?> thisClass = getClass();

        // Test token reader
        BufferedReader tokensReader = new BufferedReader(new InputStreamReader(
            thisClass.getResourceAsStream("fizzbuzz-tokens.txt")));
        InputStream is = thisClass.getResourceAsStream("fizzbuzz.il");
        TokenReader tr = new TokenReader(new InputStreamReader(is, "UTF-8"));
        int cnt = 1;
        String line;
        while ((line = tokensReader.readLine()) != null) {
            String expected = cnt + ":Token[" + line + "]";
            Token token = tr.getToken();
            assertEquals(expected, cnt + ":" + token.toString());
            cnt++;
        }
        tokensReader.close();

        // Test parser
        BufferedReader parsedReader = new BufferedReader(new InputStreamReader(
            thisClass.getResourceAsStream("fizzbuzz.il")));
        StringBuilder expected = new StringBuilder("(").append(parsedReader.readLine());
        while ((line = parsedReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0)
                expected.append(" ").append(line);
        }
        expected.append(")");
        parsedReader.close();
        is = thisClass.getResourceAsStream("fizzbuzz.il");
        tr = new TokenReader(new InputStreamReader(is, "UTF-8"));
        Engine engine = Engine.newInstance();
        Parser p = new Parser(tr, engine.getSymbolTable());
        IndyObject result = p.getExprList();
        assertEquals(expected.toString(), result.toExprString());

        // Test engine
        InputStreamReader resultReader = new InputStreamReader(
            thisClass.getResourceAsStream("fizzbuzz-result.txt"));
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

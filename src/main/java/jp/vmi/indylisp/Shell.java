package jp.vmi.indylisp;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import jp.vmi.indylisp.objects.IndyObject;

import static jp.vmi.indylisp.objects.Symbol.*;

public class Shell {

    public static void batch(String[] args) throws Exception {
        Reader r;
        if (args.length == 0)
            r = new InputStreamReader(System.in);
        else
            r = new InputStreamReader(new FileInputStream(args[0]), "UTF-8");
        Engine engine = Engine.newInstance();
        TokenReader tr = new TokenReader(r);
        Parser parser = new Parser(tr, engine.getSymbolTable());
        IndyObject expr;
        while ((expr = parser.getExpr()) != null)
            engine.eval(expr);
    }

    public static void interactive(String[] args, final Console console) throws Exception {
        final boolean[] isDebug = { false };
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-d":
                isDebug[0] = true;
                break;
            default:
                break;
            }
        }
        Reader r = new Reader() {
            private StringReader sr = null;

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (sr != null) {
                    int result = sr.read(cbuf, off, len);
                    if (result >= 0)
                        return result;
                }
                String line;
                do {
                    if ((line = console.readLine("> ")) == null) {
                        System.err.println("Killed.");
                        return -1;
                    }
                    switch (line) {
                    case "exit":
                    case "quit":
                    case "q":
                        System.err.println("Quit.");
                        return -1;
                    }
                } while (line.length() == 0);
                sr = new StringReader(line + "\n");
                return sr.read(cbuf, off, len);
            }

            @Override
            public void close() throws IOException {
                // nop
            }
        };
        Engine engine = Engine.newInstance();
        TokenReader tr = new TokenReader(r) {
            @Override
            public Token getToken() throws IOException {
                Token result = super.getToken();
                if (isDebug[0])
                    System.out.println(result);
                return result;
            }
        };
        Parser parser = new Parser(tr, engine.getSymbolTable());
        IndyObject expr;
        while ((expr = parser.getExpr()) != null) {
            IndyObject result = NIL;
            try {
                result = engine.eval(expr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("=> " + IndyObject.toExprString(result));
        }
    }

    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null)
            batch(args);
        else
            interactive(args, console);
    }
}

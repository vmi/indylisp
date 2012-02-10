package jp.vmi.indylisp;

import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShellTest {

    @Test
    public void testEval() throws Exception {
        InputStream is = ShellTest.class.getResourceAsStream("fizzbuzz.il");
        System.setIn(is);
        Shell.batch(new String[0]);
        assertTrue(true);
    }
}

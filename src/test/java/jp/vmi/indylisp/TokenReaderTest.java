package jp.vmi.indylisp;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;

import org.junit.Test;

import jp.vmi.indylisp.TokenReader.Token;
import jp.vmi.indylisp.TokenReader.TokenType;

import static org.junit.Assert.*;

public class TokenReaderTest {

    @Test
    public void testGetToken() throws IOException {
        StringReader r = new StringReader(
            "   (Symbol s-y-m-b-o-l 'qsymbol \ud800\udc00  (1 2.3 4.5.6)  \"String\"  \"Str\\\"ing\"  )  ");
        TokenReader tr = new TokenReader(r);
        Token token;

        token = tr.getToken(); // '('
        assertEquals(TokenType.OPEN, token.type);
        assertEquals("Token[OPEN]", token.toString());

        token = tr.getToken(); // 'Symbol'
        assertEquals(TokenType.SYMBOL, token.type);
        assertEquals("Symbol", token.value);
        assertEquals("Token[SYMBOL=Symbol]", token.toString());

        token = tr.getToken(); // 's-y-m-b-o-l'
        assertEquals(TokenType.SYMBOL, token.type);
        assertEquals("s-y-m-b-o-l", token.value);

        token = tr.getToken(); // "'qsymbol"
        assertEquals(TokenType.QUOTE, token.type);
        token = tr.getToken();
        assertEquals("qsymbol", token.value);

        token = tr.getToken(); // '\ud800\udc00'
        assertEquals(TokenType.SYMBOL, token.type);
        assertEquals("\ud800\udc00", token.value);

        token = tr.getToken(); // '('
        assertEquals(TokenType.OPEN, token.type);

        token = tr.getToken(); // '1'
        assertEquals(TokenType.NUMBER, token.type);
        assertEquals(new BigDecimal("1"), token.value);

        token = tr.getToken(); // '2.3'
        assertEquals(TokenType.NUMBER, token.type);
        assertEquals(new BigDecimal("2.3"), token.value);

        token = tr.getToken(); // '4.5.6'
        assertEquals(TokenType.SYMBOL, token.type);
        assertEquals("4.5.6", token.value);

        token = tr.getToken(); // ')'
        assertEquals(TokenType.CLOSE, token.type);

        token = tr.getToken(); // '"String"'
        assertEquals(TokenType.STRING, token.type);
        assertEquals("String", token.value);

        token = tr.getToken(); // '"Str"ing"'
        assertEquals(TokenType.STRING, token.type);
        assertEquals("Str\"ing", token.value);

        token = tr.getToken(); // ')'
        assertEquals(TokenType.CLOSE, token.type);

        token = tr.getToken();
        assertEquals(TokenType.EOF, token.type);

        token = tr.getToken();
        assertEquals(TokenType.EOF, token.type);
    }
}

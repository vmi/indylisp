package jp.vmi.indylisp;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;

public class TokenReader {

    public enum TokenType {
        OPEN, CLOSE, QUOTE, STRING, NUMBER, SYMBOL, EOF
    }

    public static class Token {
        public TokenType type;
        public Object value;

        public Token(TokenType type, Object value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + type + ((value != null) ? "=" + value + "]" : "]");
        }
    }

    private static Token TOKEN_OPEN = new Token(TokenType.OPEN, null);
    private static Token TOKEN_CLOSE = new Token(TokenType.CLOSE, null);
    private static Token TOKEN_QUOTE = new Token(TokenType.QUOTE, null);
    private static Token TOKEN_EOF = new Token(TokenType.EOF, null);

    private static final int NO_UNGET_CHAR = -2;

    private Reader r = null;
    private int ungetChar = NO_UNGET_CHAR;

    public TokenReader(Reader r) {
        this.r = r;

    }

    public void setReader(Reader r) {
        this.r = r;
        ungetChar = NO_UNGET_CHAR;
    }

    public Reader getReader() {
        return r;
    }

    private int getCodePoint() throws IOException {
        if (ungetChar != NO_UNGET_CHAR) {
            int c = ungetChar;
            ungetChar = NO_UNGET_CHAR;
            return c;
        }
        int c = r.read();
        if (c == -1) {
            try {
                r.close();
            } catch (IOException e) {
                // nop
            }
            return c;
        }
        if (!Character.isSurrogate((char) c))
            return c;
        if (!Character.isHighSurrogate((char) c))
            throw new IOException("Invalid unicode sequence");
        int d = r.read();
        if (d == -1) {
            try {
                r.close();
            } catch (IOException e) {
                // nop
            }
            throw new IOException("Invalid unicode sequence");
        }
        if (!Character.isLowSurrogate((char) d))
            throw new IOException("Invalid unicode sequence");
        return Character.toCodePoint((char) c, (char) d);
    }

    public Token getToken() throws IOException {
        try {
            while (true) {
                int c = getCodePoint();
                if (c == -1) {
                    return TOKEN_EOF;
                }
                else if (Character.isWhitespace(c))
                    continue;
                switch (c) {
                case ';':
                    skipComment();
                    continue;
                case '(':
                    return TOKEN_OPEN;
                case ')':
                    return TOKEN_CLOSE;
                case '"':
                    String str = readString();
                    return new Token(TokenType.STRING, str);
                case '\'':
                    return TOKEN_QUOTE;
                default:
                    String sym = readSymbol(c);
                    if (sym.matches("[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)"))
                        return new Token(TokenType.NUMBER, new BigDecimal(sym));
                    else
                        return new Token(TokenType.SYMBOL, sym);
                }
            }
        } catch (EOFException e) {
            return TOKEN_EOF;
        } catch (IOException e) {
            if ("Stream closed".equals(e.getMessage()))
                return TOKEN_EOF;
            throw e;
        }
    }

    private void skipComment() throws IOException {
        while (true) {
            int c = getCodePoint();
            switch (c) {
            case '\r':
                if ((c = getCodePoint()) != '\n')
                    ungetChar = c;
                // fall through
            case '\n':
            case -1:
                return;
            default:
                continue;
            }
        }
    }

    private String readString() throws IOException {
        StringBuilder buf = new StringBuilder();
        while (true) {
            int c = getCodePoint();
            if (c == '"')
                break;
            else if (c == '\\')
                c = getCodePoint();
            buf.appendCodePoint(c);
        }
        return buf.toString();
    }

    private String readSymbol(int c) throws IOException {
        StringBuilder buf = new StringBuilder();
        loop: while (true) {
            buf.appendCodePoint(c);
            c = getCodePoint();
            switch (c) {
            case -1:
            case '(':
            case ')':
            case '"':
            case '\'':
            case ';':
                ungetChar = c;
                break loop;
            default:
                if (Character.isWhitespace(c))
                    break loop;
                continue;
            }
        }
        return buf.toString();
    }
}

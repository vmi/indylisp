package jp.vmi.indylisp;

import java.io.IOException;
import java.math.BigDecimal;

import jp.vmi.indylisp.TokenReader.Token;
import jp.vmi.indylisp.objects.Cell;
import jp.vmi.indylisp.objects.CellList;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.NumberWrapper;
import jp.vmi.indylisp.objects.StringWrapper;
import jp.vmi.indylisp.objects.Symbol.SymbolTable;

public class Parser {

    private TokenReader tr;
    private final SymbolTable symbolTable;

    public Parser(TokenReader tr, SymbolTable symbolTable) {
        this.tr = tr;
        this.symbolTable = symbolTable;
    }

    public void setTokenReader(TokenReader tr) {
        this.tr = tr;
    }

    private IndyObject getExprList(boolean isTopLevel) throws IOException {
        CellList cellList = new CellList();
        IndyObject obj;
        while ((obj = getExpr(isTopLevel)) != null)
            cellList.append(obj);
        return cellList.toCell();
    }

    private IndyObject getExpr(boolean isTopLevel) throws IOException {
        Token token = tr.getToken();
        switch (token.type) {
        case OPEN:
            return getExprList(false);
        case CLOSE:
            if (isTopLevel)
                throw new EvalException("Unmatched close parensis.");
            return null;
        case EOF:
            if (!isTopLevel)
                throw new EvalException("Unexpected termination.");
            return null;
        case QUOTE:
            return new Cell(symbolTable.symbol("quote"), new Cell(getExpr(false)));
        case STRING:
            return StringWrapper.getInstance((String) token.value);
        case NUMBER:
            return NumberWrapper.getInstance((BigDecimal) token.value);
        case SYMBOL:
            return symbolTable.symbol((String) token.value);
        default:
            // not reached
            throw new RuntimeException("Unknown token type: " + token.type);
        }
    }

    public IndyObject getExprList() throws IOException {
        return getExprList(true);
    }

    public IndyObject getExpr() throws IOException {
        return getExpr(true);
    }
}

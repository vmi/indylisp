package jp.vmi.indylisp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jp.vmi.indylisp.annotations.SpecialForm;
import jp.vmi.indylisp.bindings.Bindings;
import jp.vmi.indylisp.bindings.BoundEntry;
import jp.vmi.indylisp.importer.Converters;
import jp.vmi.indylisp.importer.GlobalConverters;
import jp.vmi.indylisp.importer.Importer;
import jp.vmi.indylisp.objects.BufferedReaderWrapper;
import jp.vmi.indylisp.objects.Cell;
import jp.vmi.indylisp.objects.IndyObject;
import jp.vmi.indylisp.objects.Lambda;
import jp.vmi.indylisp.objects.LazyExprList;
import jp.vmi.indylisp.objects.Method;
import jp.vmi.indylisp.objects.Nil;
import jp.vmi.indylisp.objects.NumberWrapper;
import jp.vmi.indylisp.objects.PrintStreamWrapper;
import jp.vmi.indylisp.objects.StringWrapper;
import jp.vmi.indylisp.objects.Symbol;
import jp.vmi.indylisp.objects.Symbol.SymbolTable;

import static jp.vmi.indylisp.bindings.BoundEntry.*;
import static jp.vmi.indylisp.objects.Symbol.*;

public class Engine {

    public static final String NAMESPACE = "Engine";

    private static final Class<?>[] DEFAULT_IMPORTS = {
        Nil.class,
        Cell.class,
        StringWrapper.class,
        NumberWrapper.class,
        BufferedReaderWrapper.class,
        PrintStreamWrapper.class,
    };

    private final SymbolTable symbolTable = new SymbolTable();
    private final GlobalConverters globalConverters = new GlobalConverters();
    private Bindings currentBindings = new Bindings(null);
    private final List<Bindings> bindingStack = new ArrayList<>();

    private Engine() {
        // Import classes
        for (Class<?> c : DEFAULT_IMPORTS)
            Importer.importClass(this, c);
        Importer.importClass(this, getClass());
        // Setup standard in/out/err
        BufferedReaderWrapper in = new BufferedReaderWrapper(new BufferedReader(new InputStreamReader(System.in)));
        PrintStreamWrapper out = new PrintStreamWrapper(System.out);
        PrintStreamWrapper err = new PrintStreamWrapper(System.err);
        currentBindings.bind(symbol("in"), NIL, 0, in, 0);
        currentBindings.bind(symbol("out"), NIL, 0, out, 0);
        currentBindings.bind(symbol("err"), NIL, 0, err, 0);
    }

    public static Engine newInstance() {
        return new Engine();
    }

    public Symbol symbol(String name) {
        return symbolTable.symbol(name);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Converters newConverters() {
        return globalConverters.newConverters();
    }

    public Bindings getCurrentBindings() {
        return currentBindings;
    }

    public void pushBindings(Bindings newBindings) {
        bindingStack.add(currentBindings);
        currentBindings = newBindings;
    }

    public void replaceBindings(Bindings newBindings) {
        if (currentBindings.isTopLevel())
            throw new EvalException("Can't replace bindings in toplevel.");
        currentBindings = newBindings;
    }

    public void restoreBindings() {
        currentBindings = bindingStack.remove(bindingStack.size() - 1);
    }

    // Functions / Special forms

    // (quote object) -> object itself
    @SpecialForm
    public IndyObject quote(IndyObject obj) {
        return obj;
    }

    // (cond (expr1 result1) ... (exprN result N))
    @SpecialForm
    public IndyObject cond(IndyObject... caseList) {
        for (IndyObject caseElem : caseList)
            if (!eval(caseElem.car(), false).isNil())
                return progn(caseElem.cdr().toArray());
        return NIL;
    }

    // (lambda (args ...) body ...) -> defined lambda
    @SpecialForm
    public IndyObject lambda(IndyObject argNameList, IndyObject... exprList) {
        return new Lambda(currentBindings, argNameList, exprList);
    }

    // (define symbol value) -> value
    @SpecialForm
    public IndyObject define(Symbol symbol, IndyObject value) {
        int flag = 0;
        value = eval(value, false);
        if (value instanceof Lambda)
            flag = VAR_ARGS;
        return currentBindings.bind(symbol, NIL, 0, value, flag);
    }

    private void evalAllElements(IndyObject[] array) {
        int length = array.length;
        for (int i = 0; i < length; i++)
            array[i] = eval(array[i], false);
    }

    private IndyObject eval(IndyObject expr, boolean isTail) {
        if (expr.isSymbol()) {
            return currentBindings.lookup((Symbol) expr, NIL, 0).getValue();
        } else if (expr.isAtom()) {
            return expr;
        }
        // expr is Cell
        IndyObject invoker = expr.car();
        if (invoker.isNil())
            throw new EvalException("nil is not callable.");
        IndyObject[] args = expr.cdr().toArray();
        if (!invoker.isAtom())
            invoker = eval(invoker, false);
        if (invoker.isSymbol()) {
            BoundEntry entry = currentBindings.lookup((Symbol) invoker, NIL, args.length);
            if (Bindings.exists(entry)) { // call function
                invoker = entry.getValue();
                if (!entry.isSpecialForm())
                    evalAllElements(args);
            } else { // call method
                evalAllElements(args);
                Symbol namespace = symbolTable.get(args[0].getNamespace());
                entry = currentBindings.lookup((Symbol) invoker, namespace, args.length - 1);
                if (!Bindings.exists(entry))
                    throw new EvalException(namespace + ":" + invoker + " is not found.");
                invoker = entry.getValue();
            }
        }
        if (invoker instanceof Lambda) {
            Bindings newBindings = ((Lambda) invoker).assignParameters(args);
            if (isTail) {
                replaceBindings(newBindings);
                return new LazyExprList(((Lambda) invoker).getExprList());
            } else {
                try {
                    pushBindings(newBindings);
                    IndyObject result = progn(((Lambda) invoker).getExprList());
                    while (result instanceof LazyExprList)
                        result = progn(((LazyExprList) result).getExprList());
                    return result;
                } finally {
                    restoreBindings();
                }
            }
        } else if (invoker instanceof Method) {
            return ((Method) invoker).invoke(args);
        } else {
            throw new EvalException(invoker + " is not invoker.");
        }
    }

    public IndyObject eval(IndyObject expr) {
        return eval(expr, false);
    }

    public IndyObject progn(IndyObject... exprs) {
        IndyObject result = NIL;
        int tail = exprs.length - 1;
        for (int i = 0; i <= tail; i++)
            result = eval(exprs[i], i == tail && !currentBindings.isTopLevel());
        return result;
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
    }
}

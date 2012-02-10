package jp.vmi.indylisp.objects;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import jp.vmi.indylisp.EvalException;
import jp.vmi.indylisp.annotations.Converter;
import jp.vmi.indylisp.annotations.GlobalConverter;
import jp.vmi.indylisp.importer.Importer;

public class NumberWrapper extends Wrapper<BigDecimal> {

    public static final String NAMESPACE = "Number";

    private NumberWrapper(BigDecimal value) {
        super(value);
    }

    private NumberWrapper(long value) {
        super(BigDecimal.valueOf(value));
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toExprString() {
        return toString();
    }

    @GlobalConverter
    public static NumberWrapper getInstance(BigDecimal value) {
        return new NumberWrapper(value);
    }

    @GlobalConverter
    public static NumberWrapper getInstance(long value) {
        return new NumberWrapper(value);
    }

    @GlobalConverter
    public static NumberWrapper getInstance(int value) {
        return getInstance((long) value);
    }

    @GlobalConverter
    public static BigDecimal toBigDecimal(IndyObject obj) {
        if (obj instanceof NumberWrapper)
            return ((NumberWrapper) obj).getValue();
        throw new EvalException("Not number: " + obj);
    }

    @GlobalConverter
    public static long toLong(IndyObject obj) {
        if (obj instanceof NumberWrapper)
            return ((NumberWrapper) obj).getValue().longValue();
        throw new EvalException("Not number: " + obj);
    }

    @GlobalConverter
    public static int toInt(IndyObject obj) {
        if (obj instanceof NumberWrapper)
            return ((NumberWrapper) obj).getValue().intValue();
        throw new EvalException("Not number: " + obj);
    }

    @Converter
    public static RoundingMode toRoundingMode(IndyObject obj) {
        if (!obj.isSymbol())
            throw new EvalException("Not symbol: " + obj);
        switch (((Symbol) obj).getValue()) {
        case "UP":
            return RoundingMode.UP;
        case "DOWN":
            return RoundingMode.DOWN;
        case "CEILING":
            return RoundingMode.CEILING;
        case "FLOOR":
            return RoundingMode.FLOOR;
        case "HALF_UP":
            return RoundingMode.HALF_UP;
        case "HALF_DOWN":
            return RoundingMode.HALF_DOWN;
        case "HALF_EVEN":
            return RoundingMode.HALF_EVEN;
        case "UNNECESSARY":
            return RoundingMode.UNNECESSARY;
        default:
            throw new EvalException("Not RoundingMode constant: " + obj.getValue());
        }
    }

    @Converter
    public static MathContext toMathContext(IndyObject obj) {
        if (!obj.isSymbol())
            throw new EvalException("Not symbol: " + obj);
        switch (((Symbol) obj).getValue()) {
        case "DECIMAL32":
            return MathContext.DECIMAL32;
        case "DECIMAL64":
            return MathContext.DECIMAL64;
        case "DECIMAL128":
            return MathContext.DECIMAL128;
        case "UNLIMITED":
            return MathContext.UNLIMITED;
        default:
            throw new EvalException("Can't MathContext constant: " + obj.getValue());
        }
    }

    public IndyObject divide(IndyObject divisor, IndyObject symbol) {
        try {
            RoundingMode roundingMode = toRoundingMode(symbol);
            return getInstance(getValue().divide(((NumberWrapper) divisor).getValue(), roundingMode));
        } catch (EvalException e) {
            MathContext mathContext = toMathContext(symbol);
            return getInstance(getValue().divide(((NumberWrapper) divisor).getValue(), mathContext));
        }
    }

    public static void initImport(Importer importer) {
        importer.setNamespace(NAMESPACE);
        importer.setWrappedClass(BigDecimal.class);
        importer.exclude("setScale", int.class, int.class);
        importer.exclude("divide", BigDecimal.class, int.class);
        importer.exclude("divide", BigDecimal.class, int.class, int.class);
        importer.exclude("compareTo", Object.class);
        importer.excludeAllOf("valueOf");
    }
}

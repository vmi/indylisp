package jp.vmi.indylisp;

public class EvalException extends RuntimeException {

    private static final long serialVersionUID = -2480266458992910674L;

    public EvalException(String message) {
        super(message);
    }

    public EvalException(Throwable t) {
        super(t);
    }
}

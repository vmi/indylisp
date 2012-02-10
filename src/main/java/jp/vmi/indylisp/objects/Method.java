package jp.vmi.indylisp.objects;

public interface Method {
    IndyObject invoke(IndyObject... args);
}

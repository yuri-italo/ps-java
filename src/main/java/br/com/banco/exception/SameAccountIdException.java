package br.com.banco.exception;

public class SameAccountIdException extends BusinessException {
    public SameAccountIdException(String message, Throwable cause) {
        super(message, cause);
    }
}

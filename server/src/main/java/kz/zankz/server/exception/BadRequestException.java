package kz.zankz.server.exception;

// Наша ошибка: «запрос составлен неверно»
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
package kz.zankz.server.exception;

// Наша собственная ошибка: «запись не найдена»
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
package br.com.fiap.pet360.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entidade, Object id) {
        super("%s não encontrado(a) com id %s".formatted(entidade, id));
    }
}

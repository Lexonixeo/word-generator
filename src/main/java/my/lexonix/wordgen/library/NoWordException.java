package my.lexonix.wordgen.library;

public class NoWordException extends RuntimeException {
    public NoWordException(String message) {
        super(message);
    }
}

package my.lexonix.wordgen.library;

public class WordExistsException extends RuntimeException {
    public WordExistsException(String message) {
        super(message);
    }
}

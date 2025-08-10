package my.lexonix.wordgen.generator;

public class ManyAttemptsFailedException extends RuntimeException {
    public ManyAttemptsFailedException(String message) {
        super(message);
    }
}

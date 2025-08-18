package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.utility.Logger;

public class ManyAttemptsFailedException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public ManyAttemptsFailedException(String message) {
        super(message);
        log.write(this);
    }
}

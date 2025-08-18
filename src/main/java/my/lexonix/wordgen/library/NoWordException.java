package my.lexonix.wordgen.library;

import my.lexonix.wordgen.utility.Logger;

public class NoWordException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public NoWordException(String message) {
        super(message);
        log.write(this);
    }
}

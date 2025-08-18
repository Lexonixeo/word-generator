package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.utility.Logger;

public class NoModeAvailableException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public NoModeAvailableException(String message) {
        super(message);
        log.write(this);
    }
}

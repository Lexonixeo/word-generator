package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.utility.Logger;

public class NoTokenException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public NoTokenException(String message) {
        super(message);
        log.write(this);
    }
}

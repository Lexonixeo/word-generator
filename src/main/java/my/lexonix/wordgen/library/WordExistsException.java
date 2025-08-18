package my.lexonix.wordgen.library;

import my.lexonix.wordgen.utility.Logger;

public class WordExistsException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public WordExistsException(String message) {
        super(message);
        log.write(this);
    }
}

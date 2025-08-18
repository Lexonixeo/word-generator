package my.lexonix.wordgen.server;

import my.lexonix.wordgen.utility.Logger;

public class AuthorizationException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public AuthorizationException(String message) {
        super(message);
        log.write(this);
    }
}

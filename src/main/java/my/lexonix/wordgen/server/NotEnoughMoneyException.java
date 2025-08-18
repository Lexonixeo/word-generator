package my.lexonix.wordgen.server;

import my.lexonix.wordgen.utility.Logger;

public class NotEnoughMoneyException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public NotEnoughMoneyException(String message) {
        super(message);
        log.write(this);
    }
}

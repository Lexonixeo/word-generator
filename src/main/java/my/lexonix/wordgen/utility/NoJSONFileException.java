package my.lexonix.wordgen.utility;

public class NoJSONFileException extends RuntimeException {
    private static final Logger log = new Logger("exc");
    public NoJSONFileException(Exception e) {
        super(e);
        log.write(this);
    }
}

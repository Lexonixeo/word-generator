package my.lexonix.wordgen.utility;

public abstract class DoingThreadie {
    private final Thread thread;

    public DoingThreadie(String name, boolean isDaemon) {
        thread = new Thread(this::run);
        thread.setName(name);
        thread.setDaemon(isDaemon);
    }

    public abstract void run();

    public Thread getThread() {
        return thread;
    }
}

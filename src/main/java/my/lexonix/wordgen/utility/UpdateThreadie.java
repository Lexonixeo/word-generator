package my.lexonix.wordgen.utility;

public abstract class UpdateThreadie {
    private final Logger log;
    private final Thread thread;

    public UpdateThreadie(long sleepTime, String name, boolean isDaemon) {
        log = new Logger(name);
        thread = new Thread(() -> {
            boolean closed = false;
            while (!Thread.interrupted()) {
                log.write(Locale.getInstance("sys").get("log_updatethread_run"));
                update();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.write(Locale.getInstance("sys").get("log_updatethread_interrupt1"));
                    onClose();
                    closed = true;
                    log.write(Locale.getInstance("sys").get("log_updatethread_closed"));
                    break;
                }
            }
            if (!closed) {
                log.write(Locale.getInstance("sys").get("log_updatethread_interrupt2"));
                onClose();
                // closed = true;
                log.write(Locale.getInstance("sys").get("log_updatethread_closed"));
            }
        });
        thread.setName(name);
        thread.setDaemon(isDaemon);
    }

    public abstract void update();
    public abstract void onClose();

    public Thread getThread() {
        return thread;
    }
}

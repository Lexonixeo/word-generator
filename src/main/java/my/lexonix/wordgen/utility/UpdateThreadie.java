package my.lexonix.wordgen.utility;

public abstract class UpdateThreadie {
    private final Logger log;
    private final Thread thread;

    public UpdateThreadie(long sleepTime, String name, boolean isDaemon) {
        log = new Logger(name);
        thread = new Thread(() -> {
            boolean closed = false;
            while (!Thread.interrupted()) {
                log.write("Выполнение действий");
                update();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.write("Прерывание 1");
                    onClose();
                    closed = true;
                    log.write("Выключен");
                    break;
                }
            }
            if (!closed) {
                log.write("Прерывание 2");
                onClose();
                // closed = true;
                log.write("Выключен");
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

package my.lexonix.wordgen.utility;

public abstract class UpdateThreadie {
    private final Thread thread;

    public UpdateThreadie(long sleepTime, String name, boolean isDaemon) {
        thread = new Thread(() -> {
            boolean closed = false;
            while (!Thread.interrupted()) {
                Logger.write(Thread.currentThread().getName() + " update");
                update();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Logger.write(Thread.currentThread().getName() + " interrupted 1");
                    onClose();
                    closed = true;
                    break;
                }
            }
            if (!closed) {
                Logger.write(Thread.currentThread().getName() + " interrupted 2");
                onClose();
                // closed = true;
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

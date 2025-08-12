package my.lexonix.wordgen.utility;

public abstract class UpdateThreadie {
    private final Thread thread;

    public UpdateThreadie(long sleepTime, String name, boolean isDaemon) {
        thread = new Thread(() -> {
            boolean closed = false;
            while (!Thread.interrupted()) {
                update();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    onClose();
                    closed = true;
                    break;
                }
            }
            if (!closed) {
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

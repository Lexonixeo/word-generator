package my.lexonix.wordgen.server;

import my.lexonix.wordgen.utility.UpdateThreadie;

public class Server {
    private static final Thread SERVER_AUTO_UPDATE;
    private static final long SAU_TIME = 1000 * 60 * 5; // 5 min

    static {
        SERVER_AUTO_UPDATE = new UpdateThreadie(SAU_TIME, "SERVER AUTO UPDATE", true) {
            @Override
            public void update() {
                Players.savePlayers();
            }

            @Override
            public void onClose() {
                Players.savePlayers();
                Players.savePlayerDebts();
            }
        }.getThread();
    }

    public static void main() {
        Players.loadPlayerDebts();
        SERVER_AUTO_UPDATE.start();
    }
}

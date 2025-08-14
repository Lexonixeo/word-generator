package my.lexonix.wordgen.server;

import my.lexonix.wordgen.gateway.discord.DiscordBot;
import my.lexonix.wordgen.library.WordLibrary;
import my.lexonix.wordgen.utility.UpdateThreadie;

import java.io.File;

public class Server {
    private static final Thread SERVER_AUTO_UPDATE;
    private static final long SAU_TIME = 1000 * 60; // 1 min
    private static final DiscordBot bot;

    static {
        bot = new DiscordBot();

        SERVER_AUTO_UPDATE = new UpdateThreadie(SAU_TIME, "SERVER AUTO UPDATE", true) {
            @Override
            public void update() {
                Players.savePlayers();
                WordLibrary.save();
            }

            @Override
            public void onClose() {
                Players.savePlayers();
                WordLibrary.save();
                // Players.savePlayerDebts();
            }
        }.getThread();
    }

    public static void main() {
        generateDirectories();
        WordLibrary.load();
        Players.loadModerators();
        // Players.loadPlayerDebts();
        Players.loadPlayers();
        SERVER_AUTO_UPDATE.start();
        bot.launch();
    }

    private static void generateDirectories() {
        new File("data").mkdirs();
        new File("data/logs").mkdirs();
        new File("data/server").mkdirs();
        new File("data/server/library").mkdirs();
        new File("data/server/players").mkdirs();
        new File("data/tables").mkdirs();
        new File("data/texts").mkdirs();
    }

    public static void close() {
        bot.stop();
        SERVER_AUTO_UPDATE.interrupt();
    }
}

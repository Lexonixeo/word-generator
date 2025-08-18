package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.server.AuthorizationException;
import my.lexonix.wordgen.server.PlatformMode;
import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.server.Players;
import my.lexonix.wordgen.utility.DoingThreadie;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONObject;

public class DiscordBot extends JDABuilder {
    private static final Logger log = new Logger("DiscordBot");

    private JDA jda;
    private final MainListener ml = new MainListener();
    private final WordsListener wl = new WordsListener();
    private final BattleListener bl = new BattleListener();

    public static void main() {
        DiscordBot bot = new DiscordBot();
        bot.launch();
    }

    public DiscordBot() {
        log.write("Создание бота");
        JSONObject jo = Utility.getJSONObject("data/server/discord.json");

        super(jo.getString("token"), GatewayIntent.DEFAULT);
        applyDefault();

        // Включаем только те интенты, которые вам действительно нужны
        enableIntents(
                GatewayIntent.GUILD_MESSAGES, // для сообщений на сервере
                GatewayIntent.DIRECT_MESSAGES, // для личных сообщений
                GatewayIntent.MESSAGE_CONTENT // если бот читает содержимое сообщений
        );
        addEventListeners(ml);
        addEventListeners(wl);
        addEventListeners(bl);
        setActivity(Activity.playing("Введи /menu"));
    }

    public void launch() {
        log.write("Запуск бота");
        jda = build();
        try {
            jda.awaitReady(); // Важно!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        jda.updateCommands().addCommands(
                Commands.slash("profile", "Открыть свой профиль"),
                Commands.slash("read", "Узнать информацию о слове")
                        .addOption(OptionType.STRING, "word", "Интересуемое слово", true),
                Commands.slash("write", "Придумать слово")
                        .addOption(OptionType.STRING, "def", "Определение слова", false)
                        .addOption(OptionType.STRING, "word", "Само слово", false)
                        .addOption(OptionType.INTEGER, "mode", "Режим создания слова (1-4)", false),
                Commands.slash("stop", "Остановить сервер"),
                Commands.slash("help", "Узнать список команд"),
                Commands.slash("menu", "Открыть меню")
        ).queue();
    }

    public void stop() {
        log.write("Остановка бота");
        Thread stopper = new DoingThreadie("Stopper", true) {
            @Override
            public void run() {
                wl.getUpdater().interrupt();
                jda.shutdown();
                try {
                    jda.awaitShutdown(); // Ожидаем завершения
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.write("Бот выключен.");
            }
        }.getThread();
        stopper.start();
    }

    public static Player getPlayer(User user) {
        String accountID = Utility.intToHex((user.getId() + "id").hashCode());
        String password = Utility.getSHA256(user.getId()) + "pass";
        Player p;
        try {
            p = Players.getPlayer(
                    PlatformMode.DISCORD,
                    accountID,
                    Utility.getSHA256(password));
        } catch (AuthorizationException e) {
            p = Players.makeNewPlayer(
                    PlatformMode.DISCORD,
                    accountID,
                    Utility.getSHA256(password),
                    user.getName());
        }
        return p;
    }
}

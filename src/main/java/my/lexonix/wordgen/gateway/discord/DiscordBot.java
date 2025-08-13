package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONObject;

public class DiscordBot extends JDABuilder {
    private JDA jda;

    public static void main() {
        DiscordBot bot = new DiscordBot();
        bot.launch();
    }

    public DiscordBot() {
        Logger.write("[DiscordBot] Создание бота");
        JSONObject jo = Utility.getJSONObject("data/server/discord.json");

        super(jo.getString("token"), GatewayIntent.DEFAULT);
        applyDefault();

        // Включаем только те интенты, которые вам действительно нужны
        enableIntents(
                GatewayIntent.GUILD_MESSAGES, // для сообщений на сервере
                GatewayIntent.DIRECT_MESSAGES, // для личных сообщений
                GatewayIntent.MESSAGE_CONTENT // если бот читает содержимое сообщений
        );
        addEventListeners(new CommandsListener());
        setActivity(Activity.playing("Type !help"));
    }

    public void launch() {
        Logger.write("[DiscordBot] Запуск бота");
        jda = build();
    }

    public void stop() {
        Logger.write("[DiscordBot] Остановка бота");
        jda.shutdown();
        try {
            jda.awaitShutdown(); // Ожидаем завершения
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

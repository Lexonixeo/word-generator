package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.server.Server;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainListener extends ListenerAdapter {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();
        if (message.startsWith("!!")) {
            Logger.write("[CommandsListener] " + event.getAuthor().getName() + " написал " + message);
        }

        /*
        if (message.equalsIgnoreCase("!!hello")) {
            event.getChannel().sendMessage("Привет, " + event.getAuthor().getAsMention() + "!").queue();
        }
        */
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Logger.write("[CommandsListener] Использована команда " + event.getName());
        Player p = DiscordBot.getPlayer(event.getUser());
        switch (event.getName()) {
            case "menu": // случайное слово, слово дня, профиль и т.д, + профиль модератора // TODO
                break;
            case "profile":
                profile(event, p);
                break;
            case "stop":
                stop(event);
                break;
            case "help":
                help(event);
                break;
                // TODO: перевод денег и т.д.
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split("_");
        Player p = DiscordBot.getPlayer(event.getUser());
        if (buttonId[0].equals(p.getPlayerID())) {
            switch (buttonId[1]) {
                case "profile":
                    if (buttonId[2].equals("library")) {
                        profileLibrary(event, p, Integer.parseInt(buttonId[3]));
                    }
            }
        }
    }

    private void help(SlashCommandInteractionEvent event) {
        // TODO
    }

    private void profile(SlashCommandInteractionEvent event, Player player) {
        String messageBuilder =
                ":clipboard: Профиль:\n" +
                        ":bust_in_silhouette: Имя: " + player.getName() + "\n" +
                        ":identification_card: ID: " + player.getPlayerID() + "\n" +
                        ":moneybag: Баланс: " + player.getBalance() + "@\n" +
                        ":card_box: Личная библиотека: " + player.getWordsCount() + " слов\n";
        event.reply(messageBuilder).setActionRow(
                Button.primary(player.getPlayerID() + "_profile_library_0", "Личная библиотека")
        ).queue();
    }

    private void profileLibrary(ButtonInteractionEvent event, Player p, int page) {
        StringBuilder sb = new StringBuilder();
        sb.append(":books: Личная библиотека. Страница ").append(page + 1).append("\n");
        List<String> words = p.getWords(page);
        for (String word : words) {
            sb.append(word).append("\n");
        }
        event.editMessage(sb.toString()).setActionRow(
                Button.primary(p.getPlayerID() + "_profile_library_" + (page - 1), "Назад"),
                Button.primary(p.getPlayerID() + "_profile_library_" + (page + 1), "Вперед")
        ).queue();
    }

    private void stop(SlashCommandInteractionEvent event) {
        JSONObject jo = Utility.getJSONObject("data/server/discord.json");
        if (!event.getUser().getId().equals(jo.getString("ownerID"))) {
            event.reply("Бот выключился.").queue(waitingMessage -> {
                executor.execute(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    waitingMessage.editOriginal("Бот включился.").queue();
                });
            });
        } else {
            event.reply("Выключаю сервер...").queue();
            Server.close();
        }
    }
}

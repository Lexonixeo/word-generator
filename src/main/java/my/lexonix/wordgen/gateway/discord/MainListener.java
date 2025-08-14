package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.server.Players;
import my.lexonix.wordgen.server.Server;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

import java.util.ArrayList;
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
                menu(event, p);
                break;
            case "profile":
                profile(event, p);
                break;
            case "stop":
                stop(event, p);
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
                case "profile": {
                    if (buttonId.length == 2) {
                        profile(event, p);
                    } else if (buttonId[2].equals("library")) {
                        profileLibrary(event, p, Integer.parseInt(buttonId[3]));
                    }
                    break;
                }
                case "menu": {
                    menu(event, p);
                    break;
                }
                case "stop": {
                    stop(event, p);
                    break;
                }
            }
        }
    }

    private void menu(SlashCommandInteractionEvent event, Player player) {
        event.deferReply().queue();
        String messageBuilder =
                """
                Вы находитесь в меню.
                Если бот не ответил на вашу команду, попробуйте ещё раз.
                Что вы хотите сделать?
                """;
        event.getHook().editOriginal(messageBuilder).setActionRow(
                Button.primary(player.getPlayerID() + "_profile", "Открыть профиль"),
                Button.primary(player.getPlayerID() + "_wordread", "Найти случайное слово"),
                Button.primary(player.getPlayerID() + "_wordgen", "Придумать случайное слово"),
                Button.danger(player.getPlayerID() + "_stop", "Остановить сервер")
        ).queue();
    }

    private void menu(ButtonInteractionEvent event, Player player) {
        event.deferEdit().setComponents().queue();
        String messageBuilder =
                """
                Вы находитесь в меню.
                Если бот не ответил на вашу команду, попробуйте ещё раз.
                Что вы хотите сделать?
                """;
        event.getHook().editOriginal(messageBuilder).setActionRow(
                Button.primary(player.getPlayerID() + "_profile", "Профиль"),
                Button.primary(player.getPlayerID() + "_wordread", "Найти случайное слово"),
                Button.primary(player.getPlayerID() + "_wordgen", "Придумать случайное слово"),
                Button.danger(player.getPlayerID() + "_stop", "Остановить сервер")
        ).queue();
    }

    private void profile(SlashCommandInteractionEvent event, Player player) {
        event.deferReply().queue();
        String messageBuilder =
                ":clipboard: Профиль:\n" +
                        ":bust_in_silhouette: Имя: " + player.getName() + "\n" +
                        ":identification_card: ID: " + player.getPlayerID() + "\n" +
                        ":moneybag: Баланс: " + player.getBalance() + "@\n" +
                        ":card_box: Личная библиотека: " + player.getWordsCount() + " слов\n";
        event.getHook().editOriginal(messageBuilder).setActionRow(
                Button.primary(player.getPlayerID() + "_menu", "Меню"),
                Button.primary(player.getPlayerID() + "_profile_library_0", "Личная библиотека")
        ).queue();
    }

    private void profile(ButtonInteractionEvent event, Player player) {
        String messageBuilder =
                ":clipboard: Профиль:\n" +
                        ":bust_in_silhouette: Имя: " + player.getName() + "\n" +
                        ":identification_card: ID: " + player.getPlayerID() + "\n" +
                        ":moneybag: Баланс: " + player.getBalance() + "@\n" +
                        ":card_box: Личная библиотека: " + player.getWordsCount() + " слов\n";
        event.editMessage(messageBuilder).setActionRow(
                Button.primary(player.getPlayerID() + "_menu", "Меню"),
                Button.primary(player.getPlayerID() + "_profile_library_0", "Личная библиотека")
        ).queue();
    }

    private void profileLibrary(ButtonInteractionEvent event, Player p, int page) {
        StringBuilder sb = new StringBuilder();
        sb.append(":books: Личная библиотека. Страница ").append(page + 1).append("\n");
        List<String> words = p.getWords(page);
        List<Button> firstRow = new ArrayList<>();
        List<Button> secondRow = new ArrayList<>();
        List<ActionRow> rows = new ArrayList<>();
        int i = 0;
        for (String word : words) {
            sb.append(word).append("\n");
            if (i < 5) {
                firstRow.add(Button.primary(p.getPlayerID() + "_wordread_" + words.get(i), words.get(i)));
            } else {
                secondRow.add(Button.primary(p.getPlayerID() + "_wordread_" + words.get(i), words.get(i)));
            }
            i++;
        }
        if (i > 0) {
            rows.add(ActionRow.of(firstRow));
        }
        if (i > 5) {
            rows.add(ActionRow.of(secondRow));
        }

        if (page == 0) {
            rows.add(ActionRow.of(
                    Button.success(p.getPlayerID() + "_profile", "Профиль"),
                    Button.primary(p.getPlayerID() + "_profile_library_" + (page + 1), "Вперед")
            ));
        } else {
            rows.add(ActionRow.of(
                    Button.success(p.getPlayerID() + "_profile", "Профиль"),
                    Button.primary(p.getPlayerID() + "_profile_library_" + (page - 1), "Назад"),
                    Button.primary(p.getPlayerID() + "_profile_library_" + (page + 1), "Вперед")
            ));
        }
        event.editMessage(sb.toString()).setComponents(rows).queue();
    }

    private void stop(SlashCommandInteractionEvent event, Player p) {
        if (!Players.isModerator(p)) {
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
            executor.execute(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Server.close();
            });
        }
    }

    private void stop(ButtonInteractionEvent event, Player p) {
        if (!Players.isModerator(p)) {
            event.editMessage("Бот выключился.").setComponents().queue(waitingMessage -> {
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
            event.editMessage("Выключаю сервер...").setComponents().queue();
            executor.execute(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Server.close();
            });
        }
    }
}

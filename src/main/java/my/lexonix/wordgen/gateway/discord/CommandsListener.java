package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.generator.WordGenerator;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Logger;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CommandsListener extends ListenerAdapter {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Игнорируем сообщения от других ботов
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();
        Logger.write("[CommandsListener] " + event.getAuthor().getName() + " написал " + message);

        // Пример команды
        if (message.equalsIgnoreCase("!hello")) {
            event.getChannel().sendMessage("Привет, " + event.getAuthor().getAsMention() + "!").queue();
        } else if (message.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("pong!").queue();
        } else if (message.equalsIgnoreCase("!help")) {
            event.getChannel().sendMessage("Существуют команды !hello, !ping, !word, !help.").queue();
        } else if (message.equalsIgnoreCase("!word")) {
            event.getChannel().sendMessage("Создаю новое слово...").queue(waitingMessage -> {
                executor.execute(() -> {
                    try {
                        String wordSentence;
                        // Долгая операция
                        synchronized (WordGenerator.getInstance()) {
                            wordSentence = WordGenerator.makeWord(TokenizerMode.QUADRUPLE);
                        }
                        // Обновляем сообщение
                        waitingMessage.editMessage(wordSentence).queue();
                    } catch (Exception e) {
                        waitingMessage.editMessage("❌ Ошибка: " + e.getMessage()).queue();
                    }
                });
            });
        }

        // Добавьте другие команды по аналогии
    }
}

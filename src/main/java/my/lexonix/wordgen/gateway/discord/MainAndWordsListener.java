package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.gateway.WordGateway;
import my.lexonix.wordgen.library.LibraryMode;
import my.lexonix.wordgen.library.NoWordException;
import my.lexonix.wordgen.library.Word;
import my.lexonix.wordgen.library.WordLibrary;
import my.lexonix.wordgen.server.*;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.UpdateThreadie;
import my.lexonix.wordgen.utility.Utility;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class MainAndWordsListener extends ListenerAdapter {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final HashMap<Long, TempWord> tempWordHashMap = new HashMap<>();
    private final HashMap<Long, TempWordSentences> tempWordSentencesMap = new HashMap<>();
    private static final long TEMP_UPDATER = 1000 * 30; // 30 s
    private static final long MAX_TEMP_TIME = 1000 * 90; // 90 s

    private final Thread updater;

    public MainAndWordsListener() {
        updater = new UpdateThreadie(TEMP_UPDATER, "TEMP WORD UPDATER", true) {
            @Override
            public void update() {
                Collection<Long> keys;
                synchronized (tempWordHashMap) {
                    keys = new ArrayList<>(tempWordHashMap.keySet());
                }
                Collection<Long> keys2;
                synchronized (tempWordSentencesMap) {
                    keys2 = new ArrayList<>(tempWordSentencesMap.keySet());
                }

                long now = System.currentTimeMillis();

                for (long key : keys) {
                    if (now - key > MAX_TEMP_TIME) {
                        synchronized (tempWordHashMap) {
                            tempWordHashMap.remove(key);
                        }
                    }
                }
                for (long key : keys2) {
                    if (now - key > MAX_TEMP_TIME) {
                        synchronized (tempWordSentencesMap) {
                            tempWordSentencesMap.remove(key);
                        }
                    }
                }
            }

            @Override
            public void onClose() {
                tempWordHashMap.clear();
                tempWordSentencesMap.clear();
            }
        }.getThread();
        updater.start();
    }

    public Thread getUpdater() {
        return updater;
    }

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
            case "profile":
                profile(event, p);
                break;
            case "read":
                checkWord(event);
                break;
            case "write":
                generateWord(event, p);
                break;
            case "stop":
                stop(event);
                break;
            default:
                event.reply("Неизвестная команда").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split("_");
        Player p = DiscordBot.getPlayer(event.getUser());
        if (buttonId[0].equals(p.getPlayerID())) {
            switch (buttonId[1]) {
                case "wordm":
                    switch(buttonId[2]) {
                        case "1":
                            long key = Long.parseLong(buttonId[3]);
                            TempWord tw;
                            synchronized (tempWordHashMap) {
                                tw = tempWordHashMap.get(key);
                            }
                            if (tw == null) {
                                event.reply("Вы опоздали :(").setEphemeral(true).queue();
                                break;
                            }
                            makingWords(event, p, tw);
                            break;
                        case "2":
                            event.reply("Раз не хотите придумывать слово, и ладно.").queue();
                            break;
                    }
                    break;
                case "words":
                    if (buttonId[2].equals("0")) {
                        event.reply("Раз не хотите придумывать слово, и ладно.").queue();
                        break;
                    }
                    long key = Long.parseLong(buttonId[3]);
                    TempWordSentences wordSentences;
                    synchronized (tempWordSentencesMap) {
                        wordSentences = tempWordSentencesMap.get(key);
                    }
                    if (wordSentences == null) {
                        event.reply("Вы опоздали :(").setEphemeral(true).queue();
                        break;
                    }
                    String wordSentence = switch (buttonId[2]) {
                        case "1" -> wordSentences.sentence1();
                        case "2" -> wordSentences.sentence2();
                        case "3" -> wordSentences.sentence3();
                        case "4" -> wordSentences.sentence4();
                        default -> throw new IllegalStateException("Unexpected value: " + buttonId[3]);
                    };
                    madeWord(event, p, wordSentence, wordSentences.mode());
                    break;
                case "profile":
                    if (buttonId[2].equals("library")) {
                        profileLibrary(event, p, Integer.parseInt(buttonId[3]));
                    }
            }
        }
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
        event.reply(sb.toString()).queue();
    }

    private void checkWord(SlashCommandInteractionEvent event) {
        executor.execute(() -> {
            String word = event.getOption("word").getAsString().toUpperCase();
            Word w;
            try {
                w = WordLibrary.getWord(word);
            } catch (NoWordException e) {
                event.reply(":x: Нет такого слова в библиотеке!").queue();
                return;
            }
            String messageBuilder =
                    ":identification_card: ID владельца: " + w.getOwnerID() + "\n" +
                            ":moneybag: Стоимость: " + w.getPrice() + " @\n" +
                            ":chart_with_upwards_trend: Прибыль: " + w.getIncome() + " @/ч\n" +
                            ":page_with_curl: Слово: \n" +
                            w.getSentence();
            event.reply(messageBuilder).queue();
        });
    }

    private void generateWord(SlashCommandInteractionEvent event, Player p) {
        event.reply("Подождите...").queue(waitingMessage -> {
            int mod = 0;
            try {
                mod = event.getOption("mode").getAsInt();
            } catch (NullPointerException ignored) {}
            TokenizerMode mode = switch (mod) {
                case 1 -> LETTERS;
                case 2 -> DOUBLE;
                case 3 -> TRIPLE;
                // case 4 -> QUADRUPLE;
                case 4 -> RANDOM;
                default -> TRIPLE;
            };

            String word = null;
            try {
                word = event.getOption("word").getAsString().toUpperCase();
            } catch (NullPointerException ignored) {}

            if (word != null && WordLibrary.isWordExists(word.toUpperCase())) {
                waitingMessage.editOriginal(":x: Слово уже существует!").queue();
                return;
            }

            String definition = null;
            try {
                definition = event.getOption("def").getAsString();
            } catch (NullPointerException ignored) {}

            LibraryMode m;
            if (word == null && definition == null) {
                m = LibraryMode.WordGen_DefGen;
            } else if (word == null) {
                m = LibraryMode.WordGen_DefHum;
            } else if (definition == null) {
                m = LibraryMode.WordHum_DefGen;
            } else {
                m = LibraryMode.WordHum_DefHum;
            }

            long cost = WordGateway.getCost(mode, word, definition, p);
            TempWord tw = new TempWord(cost, mode, word, definition, m);
            long key = System.currentTimeMillis();
            synchronized (tempWordHashMap) {
                tempWordHashMap.put(key, tw);
            }
            waitingMessage.editOriginal(":dollar: Стоимость попытки создания: " + cost + "@\nСойдёт?")
                    .setActionRow(
                            Button.success(p.getPlayerID() + "_wordm_1_" + key, "Да"),
                            Button.danger(p.getPlayerID() + "_wordm_2", "Нет")
            ).queue();
        });
    }

    private void makingWords(ButtonInteractionEvent event, Player p, TempWord w) {
        final TokenizerMode mode = w.mode();
        final String word = w.word();
        final String definition = w.definition();

        event.reply("Подождите...").setEphemeral(true).queue(waitingMessage -> {
            try {
                p.addBalance(-w.cost());
            } catch (NotEnoughMoneyException e) {
                waitingMessage.editOriginal("Недостаточно средств!").queue();
                return;
            }
            ArrayList<String> wordSentences = WordGateway.makeFourWordSentences(mode, word, definition);
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(":level_slider: Выберите одно из четырёх слов:\n");
            for (int i = 0; i < 4; i++) {
                String wordSentenceNow = wordSentences.get(i);
                messageBuilder.append((i + 1)).append(". ")
                        .append(wordSentenceNow, 0, Math.min(wordSentenceNow.length(), 250));
                if (Math.min(wordSentenceNow.length(), 250) == 250) {
                    messageBuilder.append("...");
                }
                messageBuilder.append("\n");
            }
            long key = System.currentTimeMillis();
            TempWordSentences tws = new TempWordSentences(w.lmode(),
                    wordSentences.get(0), wordSentences.get(1), wordSentences.get(2), wordSentences.get(3));
            synchronized (tempWordSentencesMap) {
                tempWordSentencesMap.put(key, tws);
            }
            waitingMessage.editOriginal(messageBuilder.toString()).setActionRow( // Добавляем строку с кнопками
                    Button.primary(p.getPlayerID() + "_words_1_" + key, "1"),
                    Button.primary(p.getPlayerID() + "_words_2_" + key, "2"),
                    Button.primary(p.getPlayerID() + "_words_3_" + key, "3"),
                    Button.primary(p.getPlayerID() + "_words_4_" + key, "4"),
                    Button.danger(p.getPlayerID() + "_words_0", "Никакое")
            ).queue();
        });
    }

    private void madeWord(ButtonInteractionEvent event, Player p, String wordSentence, LibraryMode mode) {
        event.reply(":tada: Поздравляем! Теперь это слово - ваше!\n" + wordSentence).queue();
        executor.execute(() -> {
            WordGateway.registerNewWord(p, wordSentence, mode);
        });
    }

    private void stop(SlashCommandInteractionEvent event) {
        JSONObject jo = Utility.getJSONObject("data/server/discord.json");
        if (!event.getUser().getId().equals(jo.getString("ownerID"))) {
            event.reply("Бот выключился.").queue(waitingMessage -> {
                executor.execute(() -> {
                    try {
                        Thread.sleep(1000);
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

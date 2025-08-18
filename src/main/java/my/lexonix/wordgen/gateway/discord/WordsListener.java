package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.generator.NoTokenException;
import my.lexonix.wordgen.library.*;
import my.lexonix.wordgen.server.*;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.UpdateThreadie;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class WordsListener extends ListenerAdapter {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final HashMap<Long, TempWord> tempWordHashMap = new HashMap<>();
    private final HashMap<Long, TempWordSentences> tempWordSentencesMap = new HashMap<>();
    private static final long TEMP_UPDATER = 1000 * 30; // 30 s
    private static final long MAX_TEMP_TIME = 1000 * 90; // 90 s
    private static final Logger log = new Logger("WordsListener");

    private final Thread updater;

    public WordsListener() {
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Player p = DiscordBot.getPlayer(event.getUser());
        switch (event.getName()) {
            case "read":
                String word = event.getOption("word").getAsString().toUpperCase();
                Word w;
                try {
                    w = WordLibrary.getWord(word);
                } catch (NoWordException e) {
                    event.reply(":x: Нет такого слова в библиотеке!").queue();
                    return;
                }
                checkWord(event, p, w);
                break;
            case "write":
                generateWord(event, p);
                break;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split("_");
        Player p = DiscordBot.getPlayer(event.getUser());
        if (buttonId[0].equals(p.getPlayerID())) {
            switch (buttonId[1]) {
                case "wordgen": {
                    generateWord(event, p);
                    break;
                }
                case "wordmaker": {
                    long key = Long.parseLong(buttonId[2]);
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
                }
                case "wordmade": {
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
                }
                case "wordread": {
                    if (buttonId.length == 2) {
                        Word w = WordLibrary.getRandomWord();
                        checkWord(event, p, w);
                        break;
                    } else {
                        Word w;
                        try {
                            w = WordLibrary.getWord(buttonId[2]);
                        } catch (NoWordException e) {
                            event.reply(":x: Нет такого слова в библиотеке!").queue();
                            return;
                        }
                        checkWord(event, p, w);
                        break;
                    }
                }
                case "wordreport": {
                    String word = buttonId[2];
                    reportWord(event, word);
                    break;
                }
                case "wordkeep": {
                    String word = buttonId[2];
                    keepWord(event, p, word);
                    break;
                }
                case "wordblock": {
                    String word = buttonId[2];
                    blockWord(event, p, word);
                    break;
                }
                case "worddelete": {
                    String word = buttonId[2];
                    removeWord(event, p, word);
                    break;
                }
                case "wordprice": {
                    String word = buttonId[2];
                    break;
                } // TODO
                case "worddef": {
                    String word = buttonId[2];
                    break;
                } // TODO
            }
        }
    }

    private void checkWord(SlashCommandInteractionEvent event, Player p, Word w) {
        executor.execute(() -> {
            String wordSentence = w.getSentence().substring(0, Math.min(w.getSentence().length(), 1500));
            String messageBuilder =
                    ":bust_in_silhouette: Имя владельца: " + Players.getName(w.getOwnerID()) + "\n" +
                            ":identification_card: ID владельца: " + w.getOwnerID() + "\n" +
                            ":moneybag: Стоимость: " + w.getPrice() + "@\n" +
                            ":chart_with_upwards_trend: Прибыль: " + w.getIncome() + "@/ч\n" +
                            ":page_with_curl: Слово: " + wordSentence;
            List<ActionRow> rows = new ArrayList<>();
            if (p.getPlayerID().equals(w.getOwnerID()) && !w.isConst()) {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_wordprice_" + w.getWord(), "Изменить стоимость").asDisabled(),
                        Button.primary(p.getPlayerID() + "_worddef_" + w.getWord(), "Изменить определение").asDisabled(),
                        Button.primary(p.getPlayerID() + "_battlesend_" + w.getWord(), "Отправить на турнир").asDisabled()
                ));
            } else if (p.getPlayerID().equals(w.getOwnerID())) {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_battlesend_" + w.getWord(), "Отправить на турнир").asDisabled()
                ));
            }
            if (Players.isModerator(p)) {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_wordread", "Случайное слово"),
                        Button.success(p.getPlayerID() + "_wordkeep_" + w.getWord(), "Оставить"),
                        Button.danger(p.getPlayerID() + "_wordblock_" + w.getWord(), "Заблокировать"),
                        Button.danger(p.getPlayerID() + "_worddelete_" + w.getWord(), "Удалить")
                ));
            } else {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_wordread", "Случайное слово"),
                        Button.danger(p.getPlayerID() + "_wordreport_" + w.getWord(), "Пожаловаться")
                ));
            }
            event.reply(messageBuilder).setComponents(rows).queue();
        });
    }

    private void checkWord(ButtonInteractionEvent event, Player p, Word w) {
        event.deferEdit().queue();
        executor.execute(() -> {
            String wordSentence = w.getSentence().substring(0, Math.min(w.getSentence().length(), 1500));
            String messageBuilder =
                    ":bust_in_silhouette: Имя владельца: " + Players.getName(w.getOwnerID()) + "\n" +
                            ":identification_card: ID владельца: " + w.getOwnerID() + "\n" +
                            ":moneybag: Стоимость: " + w.getPrice() + "@\n" +
                            ":chart_with_upwards_trend: Прибыль: " + w.getIncome() + "@/ч\n" +
                            ":page_with_curl: Слово: " + wordSentence;
            List<ActionRow> rows = new ArrayList<>();
            if (p.getPlayerID().equals(w.getOwnerID()) && !w.isConst()) {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_wordprice_" + w.getWord(), "Изменить стоимость").asDisabled(),
                        Button.primary(p.getPlayerID() + "_worddef_" + w.getWord(), "Изменить определение").asDisabled(),
                        Button.primary(p.getPlayerID() + "_battlesend_" + w.getWord(), "Отправить на турнир").asDisabled()
                ));
            } else if (p.getPlayerID().equals(w.getOwnerID())) {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_battlesend_" + w.getWord(), "Отправить на турнир").asDisabled()
                ));
            }
            if (Players.isModerator(p)) {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_menu", "Меню"),
                        Button.primary(p.getPlayerID() + "_wordread", "Случайное слово"),
                        Button.success(p.getPlayerID() + "_wordkeep_" + w.getWord(), "Оставить"),
                        Button.danger(p.getPlayerID() + "_wordblock_" + w.getWord(), "Заблокировать"),
                        Button.danger(p.getPlayerID() + "_worddelete_" + w.getWord(), "Удалить")
                ));
            } else {
                rows.add(ActionRow.of(
                        Button.primary(p.getPlayerID() + "_menu", "Меню"),
                        Button.primary(p.getPlayerID() + "_wordread", "Случайное слово"),
                        Button.danger(p.getPlayerID() + "_wordreport_" + w.getWord(), "Пожаловаться")
                ));
            }
            event.getHook().editOriginal(messageBuilder).setComponents(rows).queue();
        });
    }

    private void generateWord(SlashCommandInteractionEvent event, Player p) {
        event.deferReply().queue(waitingMessage -> {
            int mod = 3;
            try {
                mod = event.getOption("mode").getAsInt();
            } catch (NullPointerException ignored) {}
            if (mod < 1 || mod > 5) {
                mod = 3;
            }
            TokenizerMode mode = switch (mod) {
                case 1 -> LETTERS;
                case 2 -> DOUBLE;
                case 3 -> TRIPLE;
                case 4 -> QUADRUPLE;
                case 5 -> RANDOM;
                default -> TRIPLE;
            };

            String word = null;
            try {
                word = event.getOption("word").getAsString().toUpperCase();
            } catch (NullPointerException ignored) {}

            if (word != null && (WordLibrary.isWordExists(word) || WordLibrary.isWordBlocked(word))) {
                log.write("Слово " + word + " уже существует/заблокировано.");
                waitingMessage.editOriginal(":x: Слово уже существует!").queue();
                return;
            } else if (word != null && word.length() < mod && mod != 5) {
                log.write("Слово " + word + " слишком мало.");
                waitingMessage.editOriginal(":x: Слово слишком мало для создания с таким режимом!").queue();
                return;
            } else if (word != null && word.length() == 1) {
                log.write("Слово " + word + " слишком мало.");
                waitingMessage.editOriginal(":x: Слово слишком мало для создания с таким режимом!").queue();
                return;
            } else if (word != null && word.length() > 50) {
                log.write("Слово " + word + " слишком большое.");
                waitingMessage.editOriginal(":x: Слово слишком много для создания!").queue();
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

            long cost = Coster.getCost(mode, word, definition, p);
            TempWord tw = new TempWord(cost, mode, word, definition, m);
            long key = System.currentTimeMillis();
            synchronized (tempWordHashMap) {
                tempWordHashMap.put(key, tw);
            }
            waitingMessage.editOriginal(":dollar: Стоимость попытки создания: " + cost + "@\nСойдёт?")
                    .setActionRow(
                            Button.success(p.getPlayerID() + "_wordmaker_" + key, "Да"),
                            Button.danger(p.getPlayerID() + "_menu", "Нет")
            ).queue();
        });
    }

    private void generateWord(ButtonInteractionEvent event, Player p) {
        event.editMessage("Подождите, пожалуйста...").setComponents().queue(waitingMessage -> {
            long cost = Coster.getCost(TRIPLE, null, null, p);
            TempWord tw = new TempWord(cost, TRIPLE, null, null, LibraryMode.WordGen_DefGen);
            long key = System.currentTimeMillis();
            synchronized (tempWordHashMap) {
                tempWordHashMap.put(key, tw);
            }
            waitingMessage.editOriginal(":dollar: Стоимость попытки создания: " + cost + "@\nСойдёт?")
                    .setActionRow(
                            Button.success(p.getPlayerID() + "_wordmaker_" + key, "Да"),
                            Button.danger(p.getPlayerID() + "_menu", "Нет")
                    ).queue();
        });
    }

    private void makingWords(ButtonInteractionEvent event, Player p, TempWord w) {
        final TokenizerMode mode = w.mode();
        final String word = w.word();
        final String definition = w.definition();

        event.editMessage("Подождите, пожалуйста...").setComponents().queue(waitingMessage -> {
            try {
                p.addBalance(-w.cost());
            } catch (NotEnoughMoneyException e) {
                waitingMessage.editOriginal(":x: Недостаточно средств!").queue();
                return;
            }
            ArrayList<String> wordSentences;
            try {
                wordSentences = WordLibrary.makeFourWordSentences(mode, word, definition);
            } catch (NoTokenException e) {
                waitingMessage.editOriginal(":x: Текущая модель не может придумать вашему слову определение.").queue();
                p.addBalance(w.cost());
                return;
            } catch (Exception e) {
                waitingMessage.editOriginal(e.getMessage()).queue();
                p.addBalance(w.cost());
                return;
            }
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
                    Button.primary(p.getPlayerID() + "_wordmade_1_" + key, "1"),
                    Button.primary(p.getPlayerID() + "_wordmade_2_" + key, "2"),
                    Button.primary(p.getPlayerID() + "_wordmade_3_" + key, "3"),
                    Button.primary(p.getPlayerID() + "_wordmade_4_" + key, "4"),
                    Button.danger(p.getPlayerID() + "_menu", "Никакое")
            ).queue();
        });
    }

    private void madeWord(ButtonInteractionEvent event, Player p, String wordSentence, LibraryMode mode) {
        log.write("Пользователь " + p.getPlayerID() + " создал слово " + WordLibrary.getWordie(wordSentence));
        String wordSentencee = wordSentence.substring(0, Math.min(wordSentence.length(), 1800));
        event.editMessage(":tada: Поздравляем! Теперь это слово - ваше!\n" + wordSentencee).setComponents(
                ActionRow.of(
                        Button.primary(p.getPlayerID() + "_menu", "Меню")
                )
        ).queue();
        executor.execute(() -> WordLibrary.registerNewWord(p, wordSentence, mode));
    }

    private void reportWord(ButtonInteractionEvent event, String word) {
        executor.execute(() -> {
            WordLibrary.reportWord(word);
            event.reply(":incoming_envelope: Жалоба отправлена.").setEphemeral(true).queue();
        });
    }

    private void keepWord(ButtonInteractionEvent event, Player p, String word) {
        if (!Players.isModerator(p)) {
            event.reply(":x: Вы не модератор!").setEphemeral(true).queue();
            return;
        }
        executor.execute(() -> {
            WordLibrary.removeReport(word);
            event.reply(":white_check_mark: Жалоба убрана.").setEphemeral(true).queue();
        });
    }

    private void blockWord(ButtonInteractionEvent event, Player p, String word) {
        if (!Players.isModerator(p)) {
            event.reply(":x: Вы не модератор!").setEphemeral(true).queue();
            return;
        }
        executor.execute(() -> {
            WordLibrary.blockWord(word);
            event.reply(":face_with_symbols_over_mouth: Слово заблокировано.").setEphemeral(true).queue();
        });
    }

    private void removeWord(ButtonInteractionEvent event, Player p, String word) {
        if (!Players.isModerator(p)) {
            event.reply(":x: Вы не модератор!").setEphemeral(true).queue();
            return;
        }
        executor.execute(() -> {
            WordLibrary.removeWord(word);
            event.reply(":placard: Слово удалено.").setEphemeral(true).queue();
        });
    }
}

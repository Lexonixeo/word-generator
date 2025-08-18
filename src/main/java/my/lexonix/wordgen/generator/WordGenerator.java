package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Locale;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class WordGenerator {
    private final static String UPPER_LETTERS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static HashMap<String, Table> tables = new HashMap<>();
    private final static HashMap<TokenizerMode, LinkedList<String>> wordsQueueMap = new HashMap<>();
    private final static int TOKEN_LENGTH = 1000;
    private final static HashMap<TokenizerMode, Object> synchMap = new HashMap<>();
    private static final Logger log = new Logger("WordGenerator");

    static {
        synchMap.put(LETTERS, 1);
        synchMap.put(DOUBLE, 2);
        synchMap.put(TRIPLE, 3);
        synchMap.put(QUADRUPLE, 4);
        synchMap.put(RANDOM, 5);
        synchMap.put(WORDS, 6);
    }

    public static void main() {
        ArrayList<String> a = new ArrayList<>();

        a.add(WordGenerator.makeWord("Ы", LETTERS));

        Utility.saveFile("data/word.txt", a);
    }

    private static void addTable(TokenizerMode mode) {
        switch (mode) {
            case WORDS -> throw new NoModeAvailableException(Locale.getInstance("sys")
                    .get("exc_mode_notavailable").replace("{mode}", mode.toString()));
            case LETTERS -> tables.put(mode.name(), new Table("data/tables/dal1.json", true));
            case DOUBLE -> tables.put(mode.name(), new Table("data/tables/dal2.json", true));
            case TRIPLE -> tables.put(mode.name(), new Table("data/tables/dal3.json", true));
            case QUADRUPLE -> tables.put(mode.name(), new Table("data/tables/dal4.json", true));
            case RANDOM -> tables.put(mode.name(), new Table("data/tables/dalr.json", true));
        }
    }

    private static Table getTable(TokenizerMode mode) {
        log.write(Locale.getInstance("sys").get("log_wordgen_getTable").replace("{mode}", mode.toString()));
        if (!tables.containsKey(mode.name())) {
            try {
                addTable(mode);
            } catch (OutOfMemoryError e) {
                log.write(Locale.getInstance("sys").get("log_wordgen_clearingTables"));
                tables.clear();
                System.gc();
                addTable(mode);
            }
        }
        return tables.get(mode.name());
    }

    public static String makeWord(TokenizerMode mode) {
        log.write(Locale.getInstance("sys").get("log_wordgen_makeWord").replace("{mode}", mode.toString()));
        if (!wordsQueueMap.containsKey(mode)) {
            wordsQueueMap.put(mode, new LinkedList<>());
        }
        if (!wordsQueueMap.get(mode).isEmpty()) {
            return wordsQueueMap.get(mode).pollFirst();
        }
        Table t = getTable(mode);
        int attempt = 0;
        while (attempt < 5) {
            String sentence = SentenceGenerator.makeSentence(t, TOKEN_LENGTH);
            ArrayList<Token> words = Tokenizer.tokenize(sentence, WORDS);
            boolean foundFirst = false;
            StringBuilder newSentence = new StringBuilder();
            for (Token word : words) {
                if (word.toString().length() >= 2
                        && isUpper(word.toString().charAt(0))
                        &&  isUpper(word.toString().charAt(1))) { // простая проверка заглавное ли слово

                        //&& isUpper(word.toString().charAt(1))
                        //|| word.toString().length() == 1
                        //&&  isUpper(word.toString().charAt(0))) { // простая проверка заглавное ли слово
                    if (foundFirst) {
                        wordsQueueMap.get(mode).addLast(newSentence.toString());
                        newSentence = new StringBuilder();
                    }
                    foundFirst = true;
                }
                if (foundFirst) {
                    newSentence.append(word);
                    newSentence.append(Tokenizer.getSeparator(WORDS));
                }
            }
            if (wordsQueueMap.get(mode).isEmpty()) {
                attempt++;
            } else {
                break;
            }
        }
        if (wordsQueueMap.get(mode).isEmpty()) {
            throw new ManyAttemptsFailedException(Locale.getInstance("sys").get("exc_wordgen_failed"));
        } else {
            return wordsQueueMap.get(mode).pollFirst();
        }
    }

    public static String makeWord(String startWord, TokenizerMode mode) {
        log.write(Locale.getInstance("sys").get("log_wordgen_makeDef")
                .replace("{mode}", mode.toString())
                .replace("{word}", startWord));
        if (!wordsQueueMap.containsKey(mode)) {
            wordsQueueMap.put(mode, new LinkedList<>());
        }
        Table t = getTable(mode);
        int attempt = 0;
        String answer = startWord;
        while (attempt < 5) {
            String sentence = SentenceGenerator.makeSentence(t, TOKEN_LENGTH,
                    startWord + Tokenizer.getSeparator(WORDS));
            ArrayList<Token> words = Tokenizer.tokenize(sentence, WORDS);
            boolean foundFirst = false;
            boolean foundSecond = false;
            StringBuilder newSentence = new StringBuilder();
            for (Token word : words) {
                if (word.toString().length() >= 2
                        && isUpper(word.toString().charAt(0))
                        && isUpper(word.toString().charAt(1))) { // простая проверка заглавное ли слово
                    if (foundFirst && !foundSecond) {
                        foundSecond = true;
                        answer = newSentence.toString();
                        newSentence = new StringBuilder();
                    } else if (foundFirst) {
                        wordsQueueMap.get(mode).addLast(newSentence.toString());
                        newSentence = new StringBuilder();
                    }
                    foundFirst = true;
                }
                if (foundFirst) {
                    newSentence.append(word);
                    newSentence.append(Tokenizer.getSeparator(WORDS));
                }
            }
            if (foundSecond) {
                return answer;
            }
            attempt++;
        }
        throw new ManyAttemptsFailedException(Locale.getInstance("sys").get("exc_wordgen_failed"));
    }

    private static boolean isUpper(char c) {
        return UPPER_LETTERS.contains(String.valueOf(c));
    }

    public static Object getSynch(TokenizerMode mode) {
        return synchMap.get(mode);
    }
}

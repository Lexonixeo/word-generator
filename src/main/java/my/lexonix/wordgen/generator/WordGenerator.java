package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class WordGenerator {
    private final static String UPPER_LETTERS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final static HashMap<String, Table> tables = new HashMap<>();

    public static void main() {
        ArrayList<String> a = new ArrayList<>();
        /*
        a.add(WordGenerator.makeWord(TRIPLE));
        a.add(WordGenerator.makeWord(TRIPLE));
        a.add(WordGenerator.makeWord(TRIPLE));
        a.add(WordGenerator.makeWord(TRIPLE));
        a.add(WordGenerator.makeWord(TRIPLE));
        */

        a.add(WordGenerator.makeWord(RANDOM));
        a.add(WordGenerator.makeWord(RANDOM));
        a.add(WordGenerator.makeWord(RANDOM));
        a.add(WordGenerator.makeWord(RANDOM));
        a.add(WordGenerator.makeWord(RANDOM));

        //a.add(Generator.makeWord("ЯЗЫНОГВА", TRIPLE));
        Utility.saveFile("data/word.txt", a);
    }

    private static Table getTable(TokenizerMode mode) {
        if (!tables.containsKey(mode.name())) {
            switch (mode) {
                case WORDS -> throw new NoModeAvailableException("Не поддерживается мод " + mode + " для создания новых слов.");
                case LETTERS -> tables.put(mode.name(), new Table("data/tables/dal1.json"));
                case DOUBLE -> tables.put(mode.name(), new Table("data/tables/dal2.json"));
                case TRIPLE -> tables.put(mode.name(), new Table("data/tables/dal3.json"));
                case QUADRUPLE -> tables.put(mode.name(), new Table("data/tables/dal4.json"));
                case RANDOM -> tables.put(mode.name(), new Table("data/tables/dalr.json"));
            }
        }
        return tables.get(mode.name());
    }

    public static String makeWord(TokenizerMode mode) {
        Table t = getTable(mode);
        int attempt = 0;
        while (attempt < 5) {
            String sentence = SentenceGenerator.makeSentence(t, 500);
            ArrayList<Token> words = Tokenizer.tokenize(sentence, WORDS);
            boolean foundFirst = false;
            boolean foundSecond = false;
            StringBuilder newSentence = new StringBuilder();
            for (Token word : words) {
                if (word.toString().length() >= 2
                        && isUpper(word.toString().charAt(0))
                        && isUpper(word.toString().charAt(1))) { // простая проверка заглавное ли слово
                    if (foundFirst) {
                        foundSecond = true;
                        break;
                    }
                    foundFirst = true;
                }
                if (foundFirst) {
                    newSentence.append(word);
                    newSentence.append(Tokenizer.getSeparator(WORDS));
                }
            }
            if (foundSecond) {
                return newSentence.toString();
            }
            attempt++;
        }
        throw new ManyAttemptsFailedException("Не получилось создать слово :(");
    }

    public static String makeWord(String startWord, TokenizerMode mode) {
        Table t = getTable(mode);
        int attempt = 0;
        while (attempt < 5) {
            String sentence = SentenceGenerator.makeSentence(t, 500,
                    startWord + Tokenizer.getSeparator(WORDS));
            ArrayList<Token> words = Tokenizer.tokenize(sentence, WORDS);
            boolean foundFirst = false;
            boolean foundSecond = false;
            StringBuilder newSentence = new StringBuilder();
            for (Token word : words) {
                if (word.toString().length() >= 2
                        && isUpper(word.toString().charAt(0))
                        && isUpper(word.toString().charAt(1))) { // простая проверка заглавное ли слово
                    if (foundFirst) {
                        foundSecond = true;
                        break;
                    }
                    foundFirst = true;
                }
                if (foundFirst) {
                    newSentence.append(word);
                    newSentence.append(Tokenizer.getSeparator(WORDS));
                }
            }
            if (foundSecond) {
                return newSentence.toString();
            }
            attempt++;
        }
        throw new ManyAttemptsFailedException("Не получилось создать слово :(");
    }

    private static boolean isUpper(char c) {
        return UPPER_LETTERS.contains(String.valueOf(c));
    }
}

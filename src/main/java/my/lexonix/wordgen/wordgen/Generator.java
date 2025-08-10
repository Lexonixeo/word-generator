package my.lexonix.wordgen.wordgen;

import my.lexonix.wordgen.generator.SentenceGenerator;
import my.lexonix.wordgen.generator.Table;
import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;

import static my.lexonix.wordgen.tokens.TokenizerMode.TRIPLE;

public class Generator {
    private final static String UPPER_LETTERS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final static Table dal2 = new Table("data/tables/dal2.json");
    private final static Table dal3 = new Table("data/tables/dal3.json");

    public static void main(String[] args) {
        ArrayList<String> a = new ArrayList<>();
        a.add(Generator.makeWord(TRIPLE));
        a.add(Generator.makeWord("ЯЗЫНОГВА", TRIPLE));
        Utility.saveFile("data/word.txt", a);
    }

    public static String makeWord(TokenizerMode mode) {
        Table t = switch (mode) {
            case WORDS, LETTERS, QUADRUPLE, RANDOM -> throw new RuntimeException("Не поддерживается мод " + mode + " для создания новых слов.");
            case DOUBLE -> dal2;
            case TRIPLE -> dal3;
        };
        int attempt = 0;
        while (attempt < 5) {
            String sentence = SentenceGenerator.makeSentence(t, 500);
            ArrayList<Token> words = Tokenizer.tokenize(sentence, TokenizerMode.WORDS);
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
                    newSentence.append(Tokenizer.getSeparator(TokenizerMode.WORDS));
                }
            }
            if (foundSecond) {
                return newSentence.toString();
            }
            attempt++;
        }
        throw new RuntimeException("Не получилось создать слово :(");
    }

    public static String makeWord(String startWord, TokenizerMode mode) {
        Table t = switch (mode) {
            case WORDS, LETTERS, QUADRUPLE, RANDOM -> throw new RuntimeException("Не поддерживается мод " + mode + " для создания новых слов.");
            case DOUBLE -> dal2;
            case TRIPLE -> dal3;
        };
        int attempt = 0;
        while (attempt < 5) {
            String sentence = SentenceGenerator.makeSentence(t, 500,
                    startWord + Tokenizer.getSeparator(TokenizerMode.WORDS));
            ArrayList<Token> words = Tokenizer.tokenize(sentence, TokenizerMode.WORDS);
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
                    newSentence.append(Tokenizer.getSeparator(TokenizerMode.WORDS));
                }
            }
            if (foundSecond) {
                return newSentence.toString();
            }
            attempt++;
        }
        throw new RuntimeException("Не получилось создать слово :(");
    }

    private static boolean isUpper(char c) {
        return UPPER_LETTERS.contains(String.valueOf(c));
    }
}

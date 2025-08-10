package my.lexonix.wordgen.wordgen;

import my.lexonix.wordgen.generator.SentenceGenerator;
import my.lexonix.wordgen.generator.Table;
import my.lexonix.wordgen.tokens.TokenizerMode;

public class Generator {
    private final static Table dal1 = new Table("data/tables/dal1.txt");
    private final static Table dal2 = new Table("data/tables/dal2.txt");
    private final static Table dal3 = new Table("data/tables/dal3.txt");
    private final static Table dal4 = new Table("data/tables/dal4.txt");

    /*
    public static String makeWord(TokenizerMode mode) {
        Table t = switch (mode) {
            case WORDS -> throw new RuntimeException("Не поддерживается мод WORDS для создания новых слов.");
            case LETTERS -> dal1;
            case DOUBLE -> dal2;
            case TRIPLE -> dal3;
            case QUADRUPLE -> dal4;
        };
        boolean madeWord = false;
        while (!madeWord) {
            String sentence = SentenceGenerator.makeSentence(t, 500);
        }
    }
     */

    public static String makeWord(String word, TokenizerMode mode) {
        return null;
    }
}

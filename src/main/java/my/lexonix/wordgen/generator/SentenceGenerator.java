package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;

public class SentenceGenerator {
    public static String makeSentence(Table table, int tokensLength) {
        return makeSentence(table, tokensLength, table.getRandomFirstToken(), true);
    }

    public static String makeSentence(Table table, int tokensLength, String begin) {
        return begin + makeSentence(table, tokensLength, Tokenizer.getLastToken(begin, table.getMode()), false);
    }

    public static String makeSentence(Table table, int tokensLength, Token firstToken, boolean showFirstToken) {
        StringBuilder sb = new StringBuilder();

        Token nextToken = firstToken;
        if (showFirstToken) {
            sb.append(nextToken);
            sb.append(Tokenizer.getSeparator(table.getMode()));
        }

        for (int i = 1; i < tokensLength; i++) {
            nextToken = table.getRandomToken(nextToken);
            sb.append(nextToken);
            sb.append(Tokenizer.getSeparator(table.getMode()));
        }

        return sb.toString();
    }

    public static void saveSentence(Table table, String path, int tokensLength) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength));
        Utility.saveFile(path, strings);
    }

    public static void saveSentence(Table table, String path, int tokensLength, Token firstToken, boolean showFirstToken) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength, firstToken, showFirstToken));
        Utility.saveFile(path, strings);
    }

    public static void saveSentence(Table table, String path, int tokensLength, String begin) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength, begin));
        Utility.saveFile(path, strings);
    }
}

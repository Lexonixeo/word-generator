package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;

public class SentenceGenerator {
    public static String makeSentence(Table table, int tokensLength) {
        return makeSentence(table, tokensLength, table.getRandomFirstToken());
    }

    public static String makeSentence(Table table, int tokensLength, Token lastToken) {
        StringBuilder sb = new StringBuilder();

        Token nextToken = lastToken;
        sb.append(nextToken);
        sb.append(Tokenizer.getSeparator(table.getMode()));

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

    public static void saveSentence(Table table, String path, int tokensLength, Token lastToken) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength, lastToken));
        Utility.saveFile(path, strings);
    }
}

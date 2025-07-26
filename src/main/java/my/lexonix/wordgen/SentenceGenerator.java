package my.lexonix.wordgen;

import java.util.ArrayList;

public class SentenceGenerator {
    public static void makeSentence(Table table, String path, Token firstToken, int length) {
        StringBuilder sb = new StringBuilder();

        Token nextToken = firstToken;
        sb.append(nextToken);
        sb.append(Word.SEPARATOR);

        for (int i = 1; i < length; i++) {
            nextToken = table.getRandomToken(nextToken);
            sb.append(nextToken);
            sb.append(Word.SEPARATOR);
        }

        ArrayList<String> strings = new ArrayList<>();
        strings.add(sb.toString());
        Table.saveFile(path, strings);
    }
}

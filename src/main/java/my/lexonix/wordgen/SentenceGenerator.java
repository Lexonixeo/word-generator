package my.lexonix.wordgen;

import java.security.SecureRandom;
import java.util.ArrayList;

public class SentenceGenerator {
    public static void makeSentence(Table table, String path, Letter firstLetter, int length) {
        StringBuilder sb = new StringBuilder();

        Token nextToken = firstLetter;
        sb.append(nextToken);
        sb.append(Letter.SEPARATOR);

        for (int i = 1; i < length; i++) {
            nextToken = table.getRandomToken(nextToken);
            sb.append(nextToken);
            sb.append(Letter.SEPARATOR);
        }

        ArrayList<String> strings = new ArrayList<>();
        strings.add(sb.toString());
        Table.saveFile(path, strings);
    }
}

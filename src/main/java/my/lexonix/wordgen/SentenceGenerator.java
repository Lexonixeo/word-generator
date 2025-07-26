package my.lexonix.wordgen;

import java.util.ArrayList;

public class SentenceGenerator {
    public static String makeSentence(Table table, int tokensLength) {
        StringBuilder sb = new StringBuilder();

        /*
        sb.append("Стат");
        Token nextToken = new SimpleToken("ья ");
         */

        Token nextToken = table.getRandomFirstToken();
        sb.append(nextToken);
        sb.append(Tokenizer.SEPARATOR);

        for (int i = 1; i < tokensLength; i++) {
            nextToken = table.getRandomToken(nextToken);
            sb.append(nextToken);
            sb.append(Tokenizer.SEPARATOR);
        }

        return sb.toString();
    }

    public static void saveSentence(Table table, String path, int tokensLength) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength));
        Table.saveFile(path, strings);
    }
}

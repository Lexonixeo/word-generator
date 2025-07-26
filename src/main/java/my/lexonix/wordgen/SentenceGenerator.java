package my.lexonix.wordgen;

import java.util.ArrayList;

public class SentenceGenerator {
    public static void makeSentence(Table table, String path, int length) {
        StringBuilder sb = new StringBuilder();

        sb.append("Стат");
        Token nextToken = new SimpleToken("ья ");

        //Token nextToken = table.getRandomFirstToken();
        sb.append(nextToken);
        sb.append(Tokenizer.SEPARATOR);

        for (int i = 1; i < length; i++) {
            nextToken = table.getRandomToken(nextToken);
            sb.append(nextToken);
            sb.append(Tokenizer.SEPARATOR);
        }

        ArrayList<String> strings = new ArrayList<>();
        strings.add(sb.toString());
        Table.saveFile(path, strings);
    }
}

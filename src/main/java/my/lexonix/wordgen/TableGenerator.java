package my.lexonix.wordgen;

import java.util.ArrayList;

public class TableGenerator {
    public static void updateTable(Table table, String path) {
        ArrayList<String> strings = Table.readFile(path);
        String s = arrToString(strings);
        ArrayList<Token> tokens = Tokenizer.tokenize(s, table.getMode());
        for (int i = 1; i < tokens.size(); i++) {
            table.addPair(tokens.get(i-1), tokens.get(i));
        }
    }

    private static String arrToString(ArrayList<String> s) {
        StringBuilder sb = new StringBuilder();
        for (String str : s) {
            sb.append(str);
            sb.append(" ");
        }
        return sb.toString();
    }
}

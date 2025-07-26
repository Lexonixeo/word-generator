package my.lexonix.wordgen;

import java.util.ArrayList;

public class TableGenerator {
    public static void updateTable(Table table, String path) {
        ArrayList<String> strings = Table.readFile(path);
        String s = arrToString(strings);
        for (int i = 1; i < s.length(); i++) {
            table.addPair(new Letter(s.charAt(i-1)), new Letter(s.charAt(i)));
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

package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;

public class TableGenerator {
    public static void updateTable(Table table, String path) {
        ArrayList<String> strings = Utility.readFile(path);
        String s = Utility.arrToString(strings);
        ArrayList<Token> tokens = Tokenizer.tokenize(s, table.getMode());
        for (int i = 1; i < tokens.size(); i++) {
            table.addPair(tokens.get(i-1), tokens.get(i));
        }
    }
}

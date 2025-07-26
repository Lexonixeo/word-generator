package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Pair;
import my.lexonix.wordgen.utility.RandomCollection;
import my.lexonix.wordgen.utility.Utility;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class Table {
    private final HashMap<Token, HashMap<Token, Integer>> table;
    private final HashMap<Token, Integer> sumTable;
    private final String path;
    private final SecureRandom random = new SecureRandom();
    private final TokenizerMode mode;

    public Table(String path) {
        Pair<HashMap<Token, HashMap<Token, Integer>>, TokenizerMode> temp = readTable(path);
        table = temp.first();
        sumTable = countSumTable(table);
        mode = temp.second();
        this.path = path;
    }

    public Table(String path, TokenizerMode mode) {
        table = new HashMap<>();
        sumTable = new HashMap<>();
        this.mode = mode;
        this.path = path;
    }

    public TokenizerMode getMode() {
        return mode;
    }

    public Token getRandomFirstToken() {
        RandomCollection<Token> rc = new RandomCollection<>(random);
        for (Token t : table.keySet()) {
            rc.add(sumTable.get(t), t);
        }
        return rc.next();
    }

    public Token getRandomToken(Token before) {
        if (!table.containsKey(before)) {
            throw new NoTokenException("В текущей модели отсутствует продолжение для токена " + before);
        }

        RandomCollection<Token> rc = new RandomCollection<>(random);
        for (Token t : table.get(before).keySet()) {
            rc.add(table.get(before).get(t), t);
        }
        return rc.next();
    }

    public void addPair(Token before, Token token) {
        if (!table.containsKey(before)) {
            table.put(before, new HashMap<>());
            sumTable.put(before, 0);
        }
        if (!table.get(before).containsKey(token)) {
            table.get(before).put(token, 0);
        }
        table.get(before).put(token, table.get(before).get(token) + 1);
        sumTable.put(before, sumTable.get(before) + 1);
    }

    public void saveTable() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(mode.name());
        for (Token firstToken : table.keySet()) {
            StringBuilder sb = new StringBuilder();
            ArrayList<Integer> ints = new ArrayList<>();

            sb.append(firstToken);
            sb.append(Tokenizer.getSeparator(mode));

            for (Token t : table.get(firstToken).keySet()) {
                sb.append(t);
                sb.append(Tokenizer.getSeparator(mode));
                ints.add(table.get(firstToken).get(t));
            }

            strings.add(sb.toString());
            strings.add(Utility.toIntString(ints));
        }
        Utility.saveFile(path, strings);
    }

    private static HashMap<Token, Integer> countSumTable(HashMap<Token, HashMap<Token, Integer>> table) {
        HashMap<Token, Integer> sumTable = new HashMap<>();
        for (Token firstToken : table.keySet()) {
            int sum = 0;

            for (Token t : table.get(firstToken).keySet()) {
                sum += table.get(firstToken).get(t);
            }

            sumTable.put(firstToken, sum);
        }
        return sumTable;
    }

    private static Pair<HashMap<Token, HashMap<Token, Integer>>, TokenizerMode> readTable(String path) {
        HashMap<Token, HashMap<Token, Integer>> table = new HashMap<>();
        ArrayList<String> strings = Utility.readFile(path);
        TokenizerMode mode = switch(strings.getFirst()) {
            case "WORDS" -> TokenizerMode.WORDS;
            case "LETTERS" -> TokenizerMode.LETTERS;
            case "DOUBLE" -> TokenizerMode.DOUBLE;
            case "TRIPLE" -> TokenizerMode.TRIPLE;
            default -> throw new IllegalStateException("Unexpected value: " + strings.getFirst());
        };
        for (int i = 1; i < strings.size(); i += 2) {
            String s = strings.get(i);
            ArrayList<Integer> ints = Utility.readIntArray(strings.get(i+1));
            ArrayList<Token> tokens = Tokenizer.tokenize(s, mode);
            Token firstToken = tokens.getFirst();
            table.put(firstToken, new HashMap<>());
            for (int j = 1; j < tokens.size(); j++) {
                table.get(firstToken).put(tokens.get(j), ints.get(j-1));
            }
        }
        return new Pair<>(table, mode);
    }
}


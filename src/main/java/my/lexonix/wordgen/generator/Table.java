package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Pair;
import my.lexonix.wordgen.utility.RandomCollection;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class Table {
    private final HashMap<Token, HashMap<Token, Integer>> table;
    private final String path;
    private final SecureRandom random = new SecureRandom();
    private final TokenizerMode mode;
    private final RandomCollection<Token> randomCollection;

    public Table(String path) {
        Logger.write("Получение таблички " + path);
        Pair<HashMap<Token, HashMap<Token, Integer>>, TokenizerMode> temp = readTableJSON(path);
        table = temp.first();

        randomCollection = new RandomCollection<>(random);
        countRandomCollection();

        mode = temp.second();
        this.path = path;
    }

    public Table(String path, TokenizerMode mode) {
        Logger.write("Новая табличка! " + path + " : " + mode);
        table = new HashMap<>();
        randomCollection = new RandomCollection<>(random);
        this.mode = mode;
        this.path = path;
    }

    public TokenizerMode getMode() {
        return mode;
    }

    public Token getRandomFirstToken() {
        return randomCollection.next();
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

    public void updateTable(String textPath) {
        Logger.write("Обновление таблички текстом " + textPath);
        int k = switch(mode) {
            case WORDS, LETTERS -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case QUADRUPLE -> 4;
            case RANDOM -> 3;
        };
        ArrayList<String> strings = Utility.readFile(textPath);
        String s = Utility.arrToString(strings);
        for (int j = 0; j < k; j++) {
            ArrayList<Token> tokens = Tokenizer.tokenize(s.substring(j), mode);
            for (int i = 1; i < tokens.size(); i++) {
                addPair(tokens.get(i-1), tokens.get(i));
            }
        }
    }

    public void saveTableJSON() {
        Logger.write("Сохранение в JSON таблицы " + path);
        JSONObject j = new JSONObject();
        j.put("m", mode.name()); // mode
        JSONArray firstTokens = new JSONArray();
        for (Token firstToken : table.keySet()) {
            JSONObject js = new JSONObject();
            js.put("f", firstToken.toString()); // tokens
            JSONArray tokens = new JSONArray();
            for (Token t : table.get(firstToken).keySet()) {
                JSONObject jso = new JSONObject();
                jso.put("t", t.toString()); // token
                jso.put("f", table.get(firstToken).get(t)); // frequency
                tokens.put(jso);
            }
            js.put("t", tokens); // tokens
            firstTokens.put(js);
        }
        j.put("f", firstTokens); // firstTokens
        int indent = switch (mode) {
            case WORDS, LETTERS, DOUBLE, TRIPLE, QUADRUPLE -> 4;
            case RANDOM -> 0;
        };
        Utility.saveJSONObject(path, j, indent);
    }

    @Deprecated
    public void saveTable() {
        Logger.write("Сохранение в TXT таблицы " + path);

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

    private void addPair(Token before, Token token) {
        if (!table.containsKey(before)) {
            table.put(before, new HashMap<>());
        }
        if (!table.get(before).containsKey(token)) {
            table.get(before).put(token, 0);
        }
        table.get(before).put(token, table.get(before).get(token) + 1);
        randomCollection.add(1, token);
    }

    private void countRandomCollection() {
        for (Token firstToken : table.keySet()) {
            int sum = 0;

            for (Token t : table.get(firstToken).keySet()) {
                sum += table.get(firstToken).get(t);
            }

            randomCollection.add(sum, firstToken);
        }
    }

    @Deprecated
    private static Pair<HashMap<Token, HashMap<Token, Integer>>, TokenizerMode> readTable(String path) {
        Logger.write("Чтение TXT таблицы " + path);
        HashMap<Token, HashMap<Token, Integer>> table = new HashMap<>();
        ArrayList<String> strings = Utility.readFile(path);
        TokenizerMode mode = TokenizerMode.valueOf(strings.getFirst());
        /*
        TokenizerMode mode = switch(strings.getFirst()) {
            case "WORDS" -> TokenizerMode.WORDS;
            case "LETTERS" -> TokenizerMode.LETTERS;
            case "DOUBLE" -> TokenizerMode.DOUBLE;
            case "TRIPLE" -> TokenizerMode.TRIPLE;
            case "QUADRUPLE" -> TokenizerMode.QUADRUPLE; // oops, i forgot it :)
            case "RANDOM" -> TokenizerMode.RANDOM;
            default -> throw new IllegalStateException("Unexpected value: " + strings.getFirst());
        };
         */
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

    private static Pair<HashMap<Token, HashMap<Token, Integer>>, TokenizerMode> readTableJSON(String path) {
        Logger.write("Чтение JSON таблицы " + path);

        HashMap<Token, HashMap<Token, Integer>> table = new HashMap<>();
        JSONObject js = Utility.getJSONObject(path);
        TokenizerMode mode = TokenizerMode.valueOf(js.getString("m"));
        /*
        TokenizerMode mode = switch(js.getString("m")) { // mode
            case "WORDS" -> TokenizerMode.WORDS;
            case "LETTERS" -> TokenizerMode.LETTERS;
            case "DOUBLE" -> TokenizerMode.DOUBLE;
            case "TRIPLE" -> TokenizerMode.TRIPLE;
            case "QUADRUPLE" -> TokenizerMode.QUADRUPLE; // oops, i forgot it :)
            case "RANDOM" -> TokenizerMode.RANDOM;
            default -> throw new IllegalStateException("Unexpected value: " + js.getString("mode"));
        };
        */
        JSONArray firstTokens = js.getJSONArray("f"); // firstTokens
        for (int i = 0; i < firstTokens.length(); i++) {
            JSONObject firstTokenJ = firstTokens.getJSONObject(i);
            Token firstToken = new Token(firstTokenJ.getString("f")); // firstToken
            table.put(firstToken, new HashMap<>());
            JSONArray tokensJ = firstTokenJ.getJSONArray("t"); // tokens
            for (int j = 0; j < tokensJ.length(); j++) {
                JSONObject jso = tokensJ.getJSONObject(j);
                Token t = new Token(jso.getString("t")); // token
                int freq = jso.getInt("f"); // frequency
                table.get(firstToken).put(t, freq);
            }
        }
        return new Pair<>(table, mode);
    }
}


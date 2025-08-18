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
    private static final Logger log = new Logger("Table");

    private final HashMap<Token, HashMap<Token, Integer>> table;
    private final HashMap<Token, RandomCollection<Token>> tableRC;
    private final String path;
    private final SecureRandom random = new SecureRandom();
    private final TokenizerMode mode;
    private final RandomCollection<Token> randomCollection;
    private final boolean isReadOnly;

    public Table(String path, boolean isReadOnly) {
        log.write("Получение таблички " + path);
        this.isReadOnly = isReadOnly;
        table = new HashMap<>();
        tableRC = new HashMap<>();
        randomCollection = new RandomCollection<>(random);
        if (!isReadOnly) {
            mode = readTableJSON(path);
        } else {
            mode = readRCTableJSON(path);
        }
        this.path = path;
    }

    public Table(String path, TokenizerMode mode) {
        log.write("Новая табличка! " + path + " : " + mode);
        table = new HashMap<>();
        tableRC = new HashMap<>();
        randomCollection = new RandomCollection<>(random);
        this.mode = mode;
        this.path = path;
        this.isReadOnly = false;
    }

    public TokenizerMode getMode() {
        return mode;
    }

    public Token getRandomFirstToken() {
        return randomCollection.next();
    }

    public Token getRandomToken(Token before) {
        if (!table.containsKey(before) && !tableRC.containsKey(before)) {
            // может быть просто взять случайный токен невзирая на before? TODO
            throw new NoTokenException("В текущей модели отсутствует продолжение для токена " + before);
        }

        if (!isReadOnly) {
            RandomCollection<Token> rc = new RandomCollection<>(random);
            for (Token t : table.get(before).keySet()) {
                rc.add(table.get(before).get(t), t);
            }
            return rc.next();
        } else {
            return tableRC.get(before).next();
        }
    }

    public void updateTable(String textPath) {
        log.write("Обновление таблички текстом " + textPath);
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
        assert !isReadOnly : 527014032;
        log.write("Сохранение в JSON таблицы " + path);
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

    /*
    @Deprecated
    public void saveTable() {
        assert !isReadOnly : 495324325;
        log.write("Сохранение в TXT таблицы " + path);

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

     */

    private void addPair(Token before, Token token) {
        assert !isReadOnly : 486549203;
        if (!table.containsKey(before)) {
            table.put(before, new HashMap<>());
        }
        if (!table.get(before).containsKey(token)) {
            table.get(before).put(token, 0);
        }
        table.get(before).put(token, table.get(before).get(token) + 1);
        randomCollection.add(1, token);
    }

    /*
    @Deprecated
    private static Pair<HashMap<Token, HashMap<Token, Integer>>, TokenizerMode> readTable(String path) {
        log.write("Чтение TXT таблицы " + path);
        HashMap<Token, HashMap<Token, Integer>> table = new HashMap<>();
        ArrayList<String> strings = Utility.readFile(path);
        TokenizerMode mode = TokenizerMode.valueOf(strings.getFirst());
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

     */

    private TokenizerMode readTableJSON(String path) {
        log.write("Чтение JSON таблицы " + path);

        JSONObject js = Utility.getJSONObject(path);
        TokenizerMode mode = TokenizerMode.valueOf(js.getString("m"));
        JSONArray firstTokens = js.getJSONArray("f"); // firstTokens
        for (int i = 0; i < firstTokens.length(); i++) {
            JSONObject firstTokenJ = firstTokens.getJSONObject(i);
            Token firstToken = new Token(firstTokenJ.getString("f")); // firstToken
            table.put(firstToken, new HashMap<>());
            JSONArray tokensJ = firstTokenJ.getJSONArray("t"); // tokens
            long sum = 0;
            for (int j = 0; j < tokensJ.length(); j++) {
                JSONObject jso = tokensJ.getJSONObject(j);
                Token t = new Token(jso.getString("t")); // token
                int freq = jso.getInt("f"); // frequency
                table.get(firstToken).put(t, freq);
                sum += freq;
            }
            randomCollection.add(sum, firstToken);
        }
        return mode;
    }

    private TokenizerMode readRCTableJSON(String path) {
        log.write("Чтение JSON таблицы (RC) " + path);

        JSONObject js = Utility.getJSONObject(path);
        TokenizerMode mode = TokenizerMode.valueOf(js.getString("m"));
        JSONArray firstTokens = js.getJSONArray("f"); // firstTokens
        for (int i = 0; i < firstTokens.length(); i++) {
            JSONObject firstTokenJ = firstTokens.getJSONObject(i);
            Token firstToken = new Token(firstTokenJ.getString("f")); // firstToken
            tableRC.put(firstToken, new RandomCollection<>(random));
            JSONArray tokensJ = firstTokenJ.getJSONArray("t"); // tokens
            long sum = 0;
            for (int j = 0; j < tokensJ.length(); j++) {
                JSONObject jso = tokensJ.getJSONObject(j);
                Token t = new Token(jso.getString("t")); // token
                int freq = jso.getInt("f"); // frequency
                tableRC.get(firstToken).add(freq, t);
                sum += freq;
            }
            randomCollection.add(sum, firstToken);
        }
        return mode;
    }
}


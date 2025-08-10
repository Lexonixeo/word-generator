package my.lexonix.wordgen.library;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class WordLibrary {
    private static final HashMap<String, String> WGDG = new HashMap<>(); // Word-Generated Definition-Generated
    private static final HashMap<String, String> WHDG = new HashMap<>();
    private static final HashMap<String, String> WGDH = new HashMap<>();
    private static final HashMap<String, String> WHDH = new HashMap<>(); // Word-Humanitated Definition-Humanitated
    private static final ArrayList<String> words = new ArrayList<>();

    private static void save(String path, HashMap<String, String> map) {
        JSONArray ja = new JSONArray();
        for (String word : map.keySet()) {
            JSONObject jo = new JSONObject();
            jo.put("w", word); // word
            jo.put("d", map.get(word)); // definition
            ja.put(jo);
        }
        Utility.saveJSONArray(path, ja);
    }

    public static void save() {
        save("data/library/wgdg.json", WGDG);
        save("data/library/whdg.json", WHDG);
        save("data/library/wgdh.json", WGDH);
        save("data/library/whdh.json", WHDH);
    }

    private static void load(String path, HashMap<String, String> map) {
        JSONArray ja = Utility.getJSONArray(path);
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            map.put(jo.getString("w"), jo.getString("d")); // word + definition
            assert !words.contains(jo.getString("w")) : 459327423;
            words.add(jo.getString("w"));
        }
    }

    public static void load() {
        load("data/library/wgdg.json", WGDG);
        load("data/library/whdg.json", WHDG);
        load("data/library/wgdh.json", WGDH);
        load("data/library/whdh.json", WHDH);
    }

    public static void addWord(String word, String def, LibraryMode mode) {
        if (words.contains(word.toUpperCase())) {
            throw new WordExistsException("Слово уже существует!");
        }
        switch (mode) {
            case WordGen_DefGen -> WGDG.put(word.toUpperCase(), def);
            case WordHum_DefHum -> WHDH.put(word.toUpperCase(), def);
            case WordGen_DefHum -> WGDH.put(word.toUpperCase(), def);
            case WordHum_DefGen -> WHDG.put(word.toUpperCase(), def);
        }
        words.add(word.toUpperCase());
    }

    public static void addWord(String sentence, LibraryMode mode) {
        Token firstToken = Tokenizer.tokenize(sentence, TokenizerMode.WORDS).getFirst();
        String word = firstToken.toString();
        String definition = sentence.substring(word.length());
        addWord(word, definition, mode);
    }
}

package my.lexonix.wordgen.library;

import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.NoJSONFileException;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;

public class WordLibrary {
    private static final HashMap<String, String> WGDG = new HashMap<>(); // Word-Generated Definition-Generated
    private static final HashMap<String, String> WHDG = new HashMap<>();
    private static final HashMap<String, String> WGDH = new HashMap<>();
    private static final HashMap<String, String> WHDH = new HashMap<>(); // Word-Humanitated Definition-Humanitated
    private static final HashMap<String, LibraryMode> WORD_MODES = new HashMap<>();
    private static final HashMap<String, String> WORD_OWNERS = new HashMap<>(); // K: Word V: PlayerID
    private static final ArrayList<String> BLOCKED_WORDS = new ArrayList<>();

    private static void save(String path, HashMap<String, String> map) {
        JSONArray ja = new JSONArray();
        for (String word : map.keySet()) {
            JSONObject jo = new JSONObject();
            jo.put("w", word); // word
            jo.put("d", map.get(word)); // definition
            jo.put("o", WORD_OWNERS.get(word)); // owner
            ja.put(jo);
        }
        Utility.saveJSONArray(path, ja);
    }

    public static void save() {
        Logger.write("[WordLibrary] Сохранение библиотеки слов");
        save("data/server/library/wgdg.json", WGDG);
        save("data/server/library/whdg.json", WHDG);
        save("data/server/library/wgdh.json", WGDH);
        save("data/server/library/whdh.json", WHDH);
    }

    private static void load(String path, HashMap<String, String> map, LibraryMode mode) {
        try {
            JSONArray ja = Utility.getJSONArray(path);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                String word = jo.getString("w"); // word
                map.put(word, jo.getString("d")); // definition
                assert !WORD_MODES.containsKey(word) : 459327423;
                WORD_MODES.put(word, mode);
                WORD_OWNERS.put(word, jo.getString("o")); // owner
            }
        } catch (NoJSONFileException e) {
            save(path, map);
        }
    }

    public static void load() {
        Logger.write("[WordLibrary] Загрузка библиотеки слов");
        load("data/server/library/wgdg.json", WGDG, LibraryMode.WordGen_DefGen);
        load("data/server/library/whdg.json", WHDG, LibraryMode.WordHum_DefGen);
        load("data/server/library/wgdh.json", WGDH, LibraryMode.WordGen_DefHum);
        load("data/server/library/whdh.json", WHDH, LibraryMode.WordHum_DefHum);
    }

    public static void addWord(String word, String def, LibraryMode mode, String playerID) {
        Logger.write("[WordLibrary] Добавление в библиотеку слова " + word);
        if (WORD_MODES.containsKey(word.toUpperCase())) {
            throw new WordExistsException("Слово уже существует!");
        }
        (switch (mode) {
            case WordGen_DefGen -> WGDG;
            case WordHum_DefHum -> WHDH;
            case WordGen_DefHum -> WGDH;
            case WordHum_DefGen -> WHDG;
        }).put(word.toUpperCase(), def);
        WORD_MODES.put(word.toUpperCase(), mode);
        WORD_OWNERS.put(word.toUpperCase(), playerID);
    }

    public static void addWord(String sentence, LibraryMode mode, String playerID) {
        Token firstToken = Tokenizer.tokenize(sentence, TokenizerMode.WORDS).getFirst();
        String word = firstToken.toString();
        String definition = sentence.substring(word.length());
        addWord(word, definition, mode, playerID);
    }

    public static String getDefinition(String word) {
        if (!WORD_MODES.containsKey(word.toUpperCase())) {
            throw new NoWordException("Не существует слова " + word);
        }
        LibraryMode mode = WORD_MODES.get(word.toUpperCase());
        return (switch (mode) {
            case WordGen_DefGen -> WGDG;
            case WordHum_DefHum -> WHDH;
            case WordGen_DefHum -> WGDH;
            case WordHum_DefGen -> WHDG;
        }).get(word);
    }

    public static String getPlayerID(String word) {
        return WORD_OWNERS.get(word.toUpperCase());
    }

    public static void blockWord(String word) {
        // TODO
    }
}

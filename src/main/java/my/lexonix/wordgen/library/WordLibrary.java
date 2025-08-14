package my.lexonix.wordgen.library;

import my.lexonix.wordgen.generator.WordGenerator;
import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;
import my.lexonix.wordgen.utility.NoJSONFileException;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import static my.lexonix.wordgen.tokens.TokenizerMode.TRIPLE;
import static my.lexonix.wordgen.tokens.TokenizerMode.WORDS;

public class WordLibrary {
    private final static long SIN_PERIOD = 1000 * 60 * 5 + 1000; // 5 минут 1 с

    private static final HashMap<String, Word> WGDG = new HashMap<>(); // Word-Generated Definition-Generated
    private static final HashMap<String, Word> WHDG = new HashMap<>();
    private static final HashMap<String, Word> WGDH = new HashMap<>();
    private static final HashMap<String, Word> WHDH = new HashMap<>(); // Word-Humanitated Definition-Humanitated
    private static final HashMap<String, LibraryMode> WORD_MODES = new HashMap<>();
    private static final ArrayList<String> WORDS = new ArrayList<>();
    private static final HashMap<String, String> WORD_OWNERS = new HashMap<>(); // K: Word V: PlayerID
    private static final ArrayList<String> BLOCKED_WORDS = new ArrayList<>();
    private static final ArrayList<String> REPORTED_WORDS = new ArrayList<>();

    private static void save(String path, HashMap<String, Word> map) {
        JSONArray ja = new JSONArray();
        for (String word : map.keySet()) {
            JSONObject jo = new JSONObject();
            assert word.equals(map.get(word).getWord()) : 924782024;
            jo.put("w", word); // word
            jo.put("d", map.get(word).getDefinition()); // definition
            assert map.get(word).getOwnerID().equals(WORD_OWNERS.get(word)) : 392523456;
            jo.put("o", map.get(word).getOwnerID()); // owner
            jo.put("i", map.get(word).getIncome()); // income
            jo.put("p", map.get(word).getPrice()); // price
            jo.put("u", map.get(word).getLastUpdate()); // lastUpdate
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
        saveBlockedWords();
        saveReportedWords();
    }

    private static void load(String path, HashMap<String, Word> map, LibraryMode mode) {
        try {
            JSONArray ja = Utility.getJSONArray(path);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                String word = jo.getString("w"); // word
                String definition = jo.getString("d"); // definition
                String ownerID = jo.getString("o"); // ownerID
                long income = jo.getLong("i"); // income
                long price = jo.getLong("p"); // price
                long lastUpdate = jo.getLong("u"); // lastUpdate
                map.put(word, new Word(word, definition, ownerID, income, price, lastUpdate));
                assert !WORD_MODES.containsKey(word) : 459327423;
                WORD_MODES.put(word, mode);
                WORD_OWNERS.put(word, ownerID);
                WORDS.add(word);
            }
        } catch (NoJSONFileException e) {
            save(path, map);
        }
    }

    private static void saveBlockedWords() {
        JSONArray ja = new JSONArray();
        for (String word : BLOCKED_WORDS) {
            ja.put(word);
        }
        Utility.saveJSONArray("data/server/library/blocked.json", ja);
    }

    private static void saveReportedWords() {
        JSONArray ja = new JSONArray();
        for (String word : REPORTED_WORDS) {
            ja.put(word);
        }
        Utility.saveJSONArray("data/server/library/reported.json", ja);
    }

    private static void loadBlockedWords() {
        try {
            JSONArray ja = Utility.getJSONArray("data/server/library/blocked.json");
            for (int i = 0; i < ja.length(); i++) {
                BLOCKED_WORDS.add(ja.getString(i));
            }
        } catch (NoJSONFileException e) {
            saveBlockedWords();
        }
    }

    private static void loadReportedWords() {
        try {
            JSONArray ja = Utility.getJSONArray("data/server/library/reported.json");
            for (int i = 0; i < ja.length(); i++) {
                REPORTED_WORDS.add(ja.getString(i));
            }
        } catch (NoJSONFileException e) {
            saveReportedWords();
        }
    }

    public static void load() {
        Logger.write("[WordLibrary] Загрузка библиотеки слов");
        load("data/server/library/wgdg.json", WGDG, LibraryMode.WordGen_DefGen);
        load("data/server/library/whdg.json", WHDG, LibraryMode.WordHum_DefGen);
        load("data/server/library/wgdh.json", WGDH, LibraryMode.WordGen_DefHum);
        load("data/server/library/whdh.json", WHDH, LibraryMode.WordHum_DefHum);
        loadBlockedWords();
        loadReportedWords();
    }

    private static void addWord(String word, String def, LibraryMode mode, String playerID) {
        Logger.write("[WordLibrary] Добавление в библиотеку слова " + word);
        String newWord = prepareWord(word);
        if (WORD_MODES.containsKey(newWord)) {
            throw new WordExistsException("Слово уже существует!");
        }
        (switch (mode) {
            case WordGen_DefGen -> WGDG;
            case WordHum_DefHum -> WHDH;
            case WordGen_DefHum -> WGDH;
            case WordHum_DefGen -> WHDG;
        }).put(newWord, new Word(newWord, def, playerID));
        WORD_MODES.put(newWord, mode);
        WORD_OWNERS.put(newWord, playerID);
        WORDS.add(word);
    }

    private static void addWord(String sentence, LibraryMode mode, String playerID) {
        Token firstToken = Tokenizer.tokenize(sentence, TokenizerMode.WORDS).getFirst();
        String word = firstToken.toString();
        String definition = sentence.substring(word.length());
        addWord(word, definition, mode, playerID);
    }

    public static Word getWord(String wor) {
        String word = prepareWord(wor);
        if (!WORD_MODES.containsKey(word)) {
            throw new NoWordException("Не существует слова " + word);
        }
        LibraryMode mode = WORD_MODES.get(word);
        return (switch (mode) {
            case WordGen_DefGen -> WGDG;
            case WordHum_DefHum -> WHDH;
            case WordGen_DefHum -> WGDH;
            case WordHum_DefGen -> WHDG;
        }).get(word);
    }

    public static String getPlayerID(String word) {
        return WORD_OWNERS.get(prepareWord(word));
    }

    public static String getWordie(String wordSentence) {
        return prepareWord(Tokenizer.tokenize(wordSentence, TokenizerMode.WORDS).getFirst().toString());
    }

    private static String removePunctuation(String word) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!Tokenizer.PUNCTUATION_ALPHABET.contains(String.valueOf(c))) {
                ans.append(c);
            }
        }
        return ans.toString();
    }

    private static String prepareWord(String word) {
        return removePunctuation(word.toUpperCase());
    }

    public static boolean isWordExists(String word) {
        return WORD_MODES.containsKey(prepareWord(word));
    }

    public static boolean isWordBlocked(String word) {
        return BLOCKED_WORDS.contains(prepareWord(word));
    }

    public static void reportWord(String wor) {
        Logger.write("[WordLibrary] Жалоба на слово " + wor);
        String word = prepareWord(wor);
        REPORTED_WORDS.add(word);
    }

    public static void removeReport(String wor) {
        Logger.write("[WordLibrary] Убрана жалоба на слово " + wor);
        String word = prepareWord(wor);
        REPORTED_WORDS.remove(word);
    }

    public static void blockWord(String wor) {
        Logger.write("[WordLibrary] Блокировка слова " + wor);
        String word = prepareWord(wor);
        BLOCKED_WORDS.add(word);
        WORD_OWNERS.remove(word);
        (switch (WORD_MODES.get(word)) {
            case WordGen_DefGen -> WGDG;
            case WordHum_DefHum -> WHDH;
            case WordGen_DefHum -> WGDH;
            case WordHum_DefGen -> WHDG;
        }).remove(word);
        WORD_MODES.remove(word);
        WORDS.remove(word);
    }

    public static void removeWord(String wor) {
        Logger.write("[WordLibrary] Удаление слова " + wor);
        String word = prepareWord(wor);
        WORD_OWNERS.remove(word);
        (switch (WORD_MODES.get(word)) {
            case WordGen_DefGen -> WGDG;
            case WordHum_DefHum -> WHDH;
            case WordGen_DefHum -> WGDH;
            case WordHum_DefGen -> WHDG;
        }).remove(word);
        WORD_MODES.remove(word);
        WORDS.remove(word);
    }

    public static long getCost(TokenizerMode mode, String word, String definition, Player p) {
        long cost;

        if (word != null && definition != null) {
            mode = TRIPLE;
            cost = 3;
        } else if (word != null) {
            cost = 8;
        } else if (definition != null) {
            cost = 6;
        } else {
            cost = 5;
        }

        cost = (long) (cost * (p.getWordsCount() + 10) / 10.0);
        cost = (long) (cost * (p.getBalance() + 100) / 100.0);

        if (word == null || definition == null) {
            switch (mode) {
                case QUADRUPLE -> cost *= 2;
                case RANDOM -> cost *= 4;
            }
        }

        cost = (long) (cost * (1 + 0.5 * Math.sin(System.currentTimeMillis() * 2 * Math.PI / SIN_PERIOD)));

        return cost;
    }

    public static long getChangeDefCost(String word) {
        Word w = WordLibrary.getWord(word);
        return 100;
    }

    public static ArrayList<String> makeFourWordSentences(TokenizerMode mode, String word, String definition) {
        ArrayList<String> ans = new ArrayList<>();
        while (ans.size() < 4) {
            String wordSentence;
            if (word != null && definition == null) {
                synchronized (WordGenerator.getSynch(mode)) {
                    wordSentence = WordGenerator.makeWord(word, mode);
                }
            } else if (word != null) {
                wordSentence = word + " " + definition;
            } else if (definition == null) {
                synchronized (WordGenerator.getSynch(mode)) {
                    wordSentence = WordGenerator.makeWord(mode);
                }
            } else {
                String wordSentenceBef;
                synchronized (WordGenerator.getSynch(mode)) {
                    wordSentenceBef = WordGenerator.makeWord(mode);
                }
                String wordd = Tokenizer.tokenize(wordSentenceBef, TokenizerMode.WORDS).getFirst().toString();
                wordSentence = wordd + " " + definition;
            }
            String wordie = WordLibrary.getWordie(wordSentence);
            if (!WordLibrary.isWordExists(wordie) && !WordLibrary.isWordBlocked(wordie)) {
                ans.add(wordSentence);
            }
        }
        return ans;
    }

    public static void registerNewWord(Player p, String wordSentence, LibraryMode mode) {
        WordLibrary.addWord(wordSentence, mode, p.getPlayerID());
        p.addWord(WordLibrary.getWordie(wordSentence));
        p.addIncome(WordLibrary.getWord(WordLibrary.getWordie(wordSentence)).getIncome());
    }

    public static Word getRandomWord() {
        SecureRandom r = new SecureRandom();
        return WordLibrary.getWord(WORDS.get(r.nextInt(WORDS.size())));
    }
}

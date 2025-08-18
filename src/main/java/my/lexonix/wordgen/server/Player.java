package my.lexonix.wordgen.server;

import my.lexonix.wordgen.library.NoWordException;
import my.lexonix.wordgen.library.WordLibrary;
import my.lexonix.wordgen.utility.Locale;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.NoJSONFileException;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Player {
    private static final long ONE_HOUR = 1000 * 60 * 60;
    private static final Logger log = new Logger("Player");

    private final String playerID;
    private final String passHash;
    private long balance;
    private final ArrayList<String> words;
    private long income;
    private long lastIncomeUpdate;
    private long lastIncome;
    private final String name;
    private String localeName;

    public Player(PlatformMode mode, String accountID, String passHash, String name) {
        String playerID = switch(mode) {
            case CONSOLE -> "c";
            case TELEGRAM -> "t";
            case DISCORD -> "d";
        };
        this.playerID = playerID + accountID;

        this.passHash = passHash;
        this.balance = 20;
        this.words = new ArrayList<>();
        this.name = name;
        this.income = 0;
        this.lastIncome = 0;
        this.lastIncomeUpdate = System.currentTimeMillis();
        this.localeName = "ru_RU";
    }

    public Player(String playerID, String passHash) {
        JSONObject json;
        try {
            json = Utility.getJSONObject("data/server/players/" + playerID + ".json");
        } catch (NoJSONFileException e) {
            throw new AuthorizationException(Locale.getInstance("sys").get("exc_player_not_exists"));
        }
        if (!json.getString("h").equals(passHash) // passHash
                || !json.getString("p").equals(playerID)) { // playerID
            throw new AuthorizationException(Locale.getInstance("sys").get("exc_player_wrong_password"));
        }
        this.playerID = playerID;
        this.passHash = passHash;
        this.balance = json.getLong("b"); // balance
        this.name = json.getString("n"); // name
        this.lastIncomeUpdate = json.getLong("u"); // lastIncomeUpdate
        this.income = 0;
        this.lastIncome = json.getLong("l"); // lastIncome
        this.localeName = json.getString("o"); // localeName

        JSONArray ja = json.getJSONArray("w"); // words
        this.words = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            String word = ja.getString(i);
            try {
                if (WordLibrary.getPlayerID(word).equals(playerID)) {
                    this.words.add(word);
                    income += WordLibrary.getWord(word).getIncome();
                }
            } catch (NullPointerException ignored) {}
        }
        checkIncome();
        words.sort(Comparator.naturalOrder());
        save();
    }

    /*
    public void checkDebt() {
        this.balance += Players.getDebt(playerID);
        Players.removeDebt(playerID);
    }
     */

    public void checkIncome() {
        long hours = (System.currentTimeMillis() - lastIncomeUpdate) / ONE_HOUR;
        lastIncomeUpdate += hours * ONE_HOUR;
        addBalance(lastIncome * hours);
        // не нада нам куча денег если чел отправил слово в турнир, и зашел как тока он закончился
        // блин, а если чел раздал всем свои слова и вышел?
        // при сохранении проверяем доход!
        // долгосрочно проблемы будут тока если у него купят/удалят слово во время отсутствия
        lastIncome = income;
    }

    public void save() {
        checkIncome();
        log.write(Locale.getInstance("sys").get("log_player_save").replace("{playerID}", playerID));
        JSONObject json = new JSONObject();
        json.put("p", playerID); // playerID
        json.put("h", passHash); // passHash
        json.put("b", balance); // balance
        json.put("n", name); // name
        words.sort(Comparator.naturalOrder());
        JSONArray ja = new JSONArray();
        for (String word : words) {
            ja.put(word);
        }
        json.put("w", ja); // words
        json.put("u", lastIncomeUpdate); // lastIncomeUpdate
        json.put("l", lastIncome); // lastIncome
        json.put("o", localeName); // localeName
        Utility.saveJSONObject("data/server/players/" + playerID + ".json", json, 4);
    }

    public boolean checkPassHash(String passHash) {
        return this.passHash.equals(passHash);
    }

    public void addIncome(long delta) {
        log.write(Locale.getInstance("sys").get("log_player_addIncome")
                .replace("{playerID}", playerID)
                .replace("{income}", String.valueOf(income))
        );
        this.income += delta;
    }

    public void addBalance(long delta) {
        log.write(Locale.getInstance("sys").get("log_player_addBalance")
                .replace("{playerID}", playerID)
                .replace("{balance}", String.valueOf(balance))
        );
        if (this.balance + delta < 0) {
            throw new NotEnoughMoneyException(Locale.getInstance("sys").get("exc_player_not_enough_money"));
        }
        this.balance += delta;
    }

    public long getBalance() {
        return balance;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    public long getWordsCount() {
        return words.size();
    }

    public void addWord(String word) {
        words.add(word);
    }

    public List<String> getWords(int page) {
        return words.subList(Math.min(Math.max(page * 10, 0), words.size()),
                Math.max(Math.min((page + 1) * 10, words.size()), 0));
    }

    public String getLocale() {
        return localeName;
    }
}

package my.lexonix.wordgen.server;

import my.lexonix.wordgen.library.WordLibrary;
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

    private final String playerID;
    private final String passHash;
    private long balance;
    private final ArrayList<String> words;
    private long income;
    private long lastIncomeUpdate;
    private String name;

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
        this.lastIncomeUpdate = System.currentTimeMillis();
    }

    public Player(String playerID, String passHash) {
        JSONObject json;
        try {
            json = Utility.getJSONObject("data/server/players/" + playerID + ".json");
        } catch (NoJSONFileException e) {
            throw new AuthorizationException("Ошибка авторизации");
        }
        if (!json.getString("h").equals(passHash) // passHash
                || !json.getString("p").equals(playerID)) { // playerID
            throw new AuthorizationException("Ошибка авторизации");
        }
        this.playerID = playerID;
        this.passHash = passHash;
        this.balance = json.getLong("b"); // balance
        this.name = json.getString("n"); // name
        this.lastIncomeUpdate = json.getLong("u"); // lastIncomeUpdate
        this.income = 0;

        JSONArray ja = json.getJSONArray("w"); // words
        this.words = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            String word = ja.getString(i);
            if (WordLibrary.getPlayerID(word).equals(playerID)) {
                this.words.add(word);
            }
            income += WordLibrary.getWord(word).getIncome();
        }
        words.sort(Comparator.naturalOrder());
    }

    public void checkDebt() {
        this.balance += Players.getDebt(playerID);
        Players.removeDebt(playerID);
    }

    public void checkIncome() {
        long hours = (System.currentTimeMillis() - lastIncomeUpdate) / ONE_HOUR;
        lastIncomeUpdate += hours * ONE_HOUR;
        addBalance(income * hours);
    }

    public void save() {
        Logger.write("[Player] Сохранение игрока " + playerID);
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
        Utility.saveJSONObject("data/server/players/" + playerID + ".json", json, 4);
    }

    public boolean checkPassHash(String passHash) {
        return this.passHash.equals(passHash);
    }

    public void addBalance(long delta) {
        Logger.write("[Player] Обновление баланса игрока " + playerID + " на " + delta);
        if (this.balance + delta < 0) {
            throw new NotEnoughMoneyException("Недостаточно средств!");
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
        return words.subList(Math.min(page * 10, words.size()), Math.min((page + 1) * 10, words.size()));
    }
}

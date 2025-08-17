package my.lexonix.wordgen.server;

import my.lexonix.wordgen.library.NoWordException;
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
    private long lastIncome;
    private final String name;

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
        this.lastIncome = json.getLong("l"); // lastIncome

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
        json.put("l", lastIncome); // lastIncome
        Utility.saveJSONObject("data/server/players/" + playerID + ".json", json, 4);
    }

    public boolean checkPassHash(String passHash) {
        return this.passHash.equals(passHash);
    }

    public void addIncome(long delta) {
        Logger.write("[Player] Обновление прибыли игрока " + playerID + " на " + delta);
        this.income += delta;
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
        return words.subList(Math.min(Math.max(page * 10, 0), words.size()),
                Math.max(Math.min((page + 1) * 10, words.size()), 0));
    }
}

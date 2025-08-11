package my.lexonix.wordgen.multiplayer;

import my.lexonix.wordgen.library.WordLibrary;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Player {
    private String playerID;
    private String passHash;
    private long balance;
    private final ArrayList<String> words;

    public Player(PlatformMode mode, String playerID, String password) {
        this.playerID = playerID;
        this.passHash = Utility.getSHA256(password);
        this.balance = 20;
        this.words = new ArrayList<>();
    }

    public Player(String playerID, String passHash) {
        JSONObject json = Utility.getJSONObject("data/server/players/" + playerID + ".json");
        if (!json.getString("h").equals(passHash) // passHash
                || !json.getString("p").equals(playerID)) { // playerID
            throw new AuthorizationException("Ошибка авторизации");
        }
        this.playerID = playerID;
        this.passHash = passHash;
        this.balance = json.getLong("b"); // balance

        JSONArray ja = json.getJSONArray("w"); // words
        this.words = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            String word = ja.getString(i);
            if (WordLibrary.getPlayerID(word).equals(playerID)) {
                this.words.add(word);
            }
        }
    }

    public void checkDebt() {
        this.balance += Server.getDebt(playerID);
        Server.removeDebt(playerID);
    }

    public void save() {
        JSONObject json = new JSONObject();
        json.put("p", playerID); // playerID
        json.put("h", passHash); // passHash
        json.put("b", balance); // balance
        JSONArray ja = new JSONArray();
        for (String word : words) {
            ja.put(word);
        }
        json.put("w", ja); // words
        Utility.saveJSONObject("data/server/players/" + playerID + ".json", json, 4);
    }

    public boolean checkPassHash(String passHash) {
        return this.passHash.equals(passHash);
    }

    public void addBalance(long delta) {
        this.balance += delta;
    }

    public long getBalance() {
        return balance;
    }

    public String getPlayerID() {
        return playerID;
    }
}

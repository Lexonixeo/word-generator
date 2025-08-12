package my.lexonix.wordgen.server;

import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Players {
    // игроки, которые были активны с последней чистки
    private static final ArrayList<Player> activePlayers = new ArrayList<>();
    private static final HashMap<String, Long> playerDebts = new HashMap<>(); // долги для выдачи игрокам

    public static void savePlayers() {
        for (Player p : activePlayers) {
            p.checkDebt();
            p.save();
        }
        activePlayers.clear();
    }

    public static void savePlayerDebts() {
        JSONArray ja = new JSONArray();
        for (String playerID : playerDebts.keySet()) {
            JSONObject jo = new JSONObject();
            jo.put("p", playerID); // playerID
            jo.put("d", playerDebts.get(playerID)); // debt
            ja.put(jo);
        }
        Utility.saveJSONArray("data/server/debts.json", ja);
    }

    public static void loadPlayerDebts() {
        JSONArray ja = Utility.getJSONArray("data/server/debts.json");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            playerDebts.put(jo.getString("p"), jo.getLong("d")); // playerID, debt
        }
    }

    public static long getDebt(String playerID) {
        if (playerDebts.containsKey(playerID)) {
            return playerDebts.get(playerID);
        } else {
            return 0;
        }
    }

    public static void removeDebt(String playerID) {
        playerDebts.remove(playerID);
    }

    public static Player makeNewPlayer(PlatformMode mode, String accountID, String password) {
        Player p = new Player(mode, accountID, Utility.getSHA256(password));
        if (Utility.isFileExists("data/server/players/" + p.getPlayerID() + ".json")) {
            throw new AuthorizationException("Пользователь уже существует!");
        }
        activePlayers.add(p);
        return p;
    }

    public static Player getPlayer(String playerID, String passHash) {
        for (Player p : activePlayers) {
            if (p.getPlayerID().equals(playerID)) {
                if (p.checkPassHash(passHash)) {
                    return p;
                } else {
                    throw new AuthorizationException("Ошибка авторизации 5380532");
                }
            }
        }
        Player p = new Player(playerID, passHash);
        activePlayers.add(p);
        return p;
    }
}

package my.lexonix.wordgen.server;

import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.NoJSONFileException;
import my.lexonix.wordgen.utility.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Players {
    // игроки, которые были активны с последней чистки
    private static final ArrayList<Player> activePlayers = new ArrayList<>();
    // private static final HashMap<String, Long> playerDebts = new HashMap<>(); // долги для выдачи игрокам
    private static final ArrayList<String> moderatorsID = new ArrayList<>();
    private static final HashMap<String, String> players = new HashMap<>(); // playerId, name;
    private static final Logger log = new Logger("Players");

    public static void savePlayers() {
        log.write("Сохранение активных игроков");
        for (Player p : activePlayers) {
            players.put(p.getPlayerID(), p.getName());
            // p.checkDebt();
            p.checkIncome();
            p.save();
        }
        activePlayers.clear();

        JSONObject jsonObject = new JSONObject();
        for (String playerID : players.keySet()) {
            jsonObject.put(playerID, players.get(playerID));
        }
        Utility.saveJSONObject("data/server/players/players.json", jsonObject, 4);
    }

    public static void loadPlayers() {
        try {
            JSONObject jsonObject = Utility.getJSONObject("data/server/players/players.json");
            for (String playerID : jsonObject.keySet()) {
                players.put(playerID, jsonObject.getString(playerID));
            }
        } catch (NoJSONFileException ignored) {}
    }

    public static void loadModerators() {
        JSONArray ja = Utility.getJSONArray("data/server/moderators.json");
        for (int i = 0; i < ja.length(); i++) {
            moderatorsID.add(ja.getString(i));
        }
    }

    /*
    public static void savePlayerDebts() {
        Logger.write("[Players] Сохранение долгов для игроков");
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
        Logger.write("[Players] Загрузка долгов для игроков");
        try {
            JSONArray ja = Utility.getJSONArray("data/server/debts.json");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                playerDebts.put(jo.getString("p"), jo.getLong("d")); // playerID, debt
            }
        } catch (NoJSONFileException e) {
            savePlayerDebts();
        }
    }

    public static void addDebt(String playerID, long debt) {
        Logger.write("[Players] Добавили в долг игроку " + debt);
        playerDebts.put(playerID, getDebt(playerID) + debt);
    }

    public static long getDebt(String playerID) {
        if (playerDebts.containsKey(playerID)) {
            return playerDebts.get(playerID);
        } else {
            return 0;
        }
    }

    public static void removeDebt(String playerID) {
        Logger.write("[Players] Убран долг игрока " + playerID);
        playerDebts.remove(playerID);
    }
     */

    public static Player makeNewPlayer(PlatformMode mode, String accountID, String passHash, String name) {
        log.write("Создание игрока " + mode + " " + accountID);
        Player p = new Player(mode, accountID, passHash, name);
        if (Utility.isFileExists("data/server/players/" + p.getPlayerID() + ".json")) {
            throw new AuthorizationException("Пользователь уже существует!");
        }
        players.put(p.getPlayerID(), p.getName());
        activePlayers.add(p);
        return p;
    }

    public static Player getPlayer(PlatformMode mode, String accountID, String passHash) {
        String playerID = switch(mode) {
            case CONSOLE -> "c";
            case TELEGRAM -> "t";
            case DISCORD -> "d";
        };
        playerID += accountID;

        log.write("Получение игрока " + playerID);
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
        players.put(p.getPlayerID(), p.getName());
        activePlayers.add(p);
        return p;
    }

    public static boolean isModerator(Player p) {
        return moderatorsID.contains(p.getPlayerID());
    }

    public static String getName(String playerID) {
        return players.get(playerID);
    }
}

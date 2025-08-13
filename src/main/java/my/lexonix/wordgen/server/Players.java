package my.lexonix.wordgen.server;

import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class Players {
    // игроки, которые были активны с последней чистки
    private static final ArrayList<Player> activePlayers = new ArrayList<>();
    // private static final HashMap<String, Long> playerDebts = new HashMap<>(); // долги для выдачи игрокам
    private static final ArrayList<String> moderatorsID = new ArrayList<>();

    public static void savePlayers() {
        Logger.write("[Players] Сохранение активных игроков");
        for (Player p : activePlayers) {
            // p.checkDebt();
            p.checkIncome();
            p.save();
        }
        activePlayers.clear();
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
        Logger.write("[Players] Создание игрока " + mode + " " + accountID);
        Player p = new Player(mode, accountID, passHash, name);
        if (Utility.isFileExists("data/server/players/" + p.getPlayerID() + ".json")) {
            throw new AuthorizationException("Пользователь уже существует!");
        }
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

        Logger.write("[Players] Получение игрока " + playerID);
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

    public boolean isModerator(Player p) {
        return false;
        // TODO
    }
}

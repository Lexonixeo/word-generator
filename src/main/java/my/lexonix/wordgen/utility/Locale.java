package my.lexonix.wordgen.utility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class Locale {
    private final String localeName;
    private final HashMap<String, String> localeMap;

    // Статическая карта для хранения созданных экземпляров
    private static final HashMap<Key, Locale> instances = new HashMap<>();
    private static final Logger log = new Logger("Locale");

    // Приватный конструктор
    private Locale(String localeName) {
        log.write("Создание локаля " + localeName);
        this.localeName = localeName;

        this.localeMap = new HashMap<>();
        JSONObject j = Utility.getJSONObject("data/" + localeName + ".json");
        for (String key : j.keySet()) {
            localeMap.put(key, j.getString(key));
        }
    }

    public String get(String key) {
        return localeMap.get(key);
    }

    // Фабричный метод вместо публичного конструктора
    public static Locale getInstance(String localeName) {
        Key key = new Key(localeName);

        // Двойная проверка для потокобезопасности
        Locale instance = instances.get(key);
        if (instance == null) {
            synchronized (Locale.class) {
                instance = instances.get(key);
                if (instance == null) {
                    instance = new Locale(localeName);
                    instances.put(key, instance);
                }
            }
        }

        return instance;
    }

    // Класс-ключ для карты
    private static class Key {
        private final String localeName;

        public Key(String localeName) {
            this.localeName = localeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(localeName, key.localeName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localeName);
        }
    }
}

package my.lexonix.wordgen.utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
// import java.util.Objects;

public class Utility {
    public static String toIntString(ArrayList<Integer> ints) {
        return ints.toString().replace("[", "").replace("]", "").replace(",", "");
    }

    public static void saveFile(String path, ArrayList<String> arr) {
        Logger.write("Сохранение ArrayList<String> " + path);
        try {
            FileWriter writer = new FileWriter(path);
            for(String str: arr) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Integer> readIntArray(String s) {
        ArrayList<String> arrayList = new ArrayList<>    (Arrays.asList(s.split(" ")));
        ArrayList<Integer> favList = new ArrayList<>();
        for(String fav:arrayList){
            favList.add(Integer.parseInt(fav.trim()));
        }
        return favList;
    }

    public static ArrayList<String> readFile(String path) {
        ArrayList<String> arr = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                arr.add(sCurrentLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return arr;
    }

    public static String arrToString(ArrayList<String> s) {
        StringBuilder sb = new StringBuilder();
        for (String str : s) {
            sb.append(str);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static JSONObject getJSONObject(String path) {
        Logger.write("Взятие JSONObject " + path);
        JSONObject ans;
        File file = new File(path);
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            ans = new JSONObject(content);
        } catch (NoSuchFileException e) {
            throw new NoJSONFileException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ans;
    }

    public static JSONArray getJSONArray(String path) {
        Logger.write("Взятие JSONArray " + path);
        JSONArray ans;
        File file = new File(path);
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            ans = new JSONArray(content);
        } catch (NoSuchFileException e) {
            throw new NoJSONFileException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ans;
    }

    public static void saveJSONObject(String path, JSONObject jo, int indent) {
        Logger.write("Сохранение JSONObject " + path);
        try (FileWriter file = new FileWriter(path)) {
            // Convert the JSONObject to a JSON string and write it
            file.write(jo.toString(indent));
            // Alternatively, for pretty printing (if using org.json):
            // file.write(jsonObject.toString(4)); // Indent with 4 spaces

            file.flush(); // Ensure all data is written to the file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveJSONArray(String path, JSONArray jo) {
        Logger.write("Сохранение JSONArray " + path);
        try (FileWriter file = new FileWriter(path)) {
            // Convert the JSONObject to a JSON string and write it
            file.write(jo.toString(4));
            // Alternatively, for pretty printing (if using org.json):
            // file.write(jsonObject.toString(4)); // Indent with 4 spaces

            file.flush(); // Ensure all data is written to the file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSHA256(String secret) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encodedhash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash); // Получаем искомую шестнадцатеричную строку!
    }

    // Пошаговое преобразование байтов в шестнадцатеричную строку
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static boolean isFileExists(String pathname) {
        return new File(pathname.replace('\\', '/')).exists();
    }

    /*
    public static int countFiles(String path) {
        return Objects.requireNonNull(new File(path).list()).length;
    }
     */
}

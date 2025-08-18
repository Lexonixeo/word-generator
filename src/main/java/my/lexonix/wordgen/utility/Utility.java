package my.lexonix.wordgen.utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Utility {
    private static final Logger log = new Logger("Utility");

    public static String toIntString(ArrayList<Integer> ints) {
        return ints.toString().replace("[", "").replace("]", "").replace(",", "");
    }

    public static void saveFile(String path, ArrayList<String> arr) {
        log.write(Locale.getInstance("sys").get("log_utility_saveFile").replace("{path}", path));
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
        log.write(Locale.getInstance("sys").get("log_utility_readFile").replace("{path}", path));
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
        // DO NOT CHANGE
        log.write("Чтение JSONObject " + path);
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
        log.write(Locale.getInstance("sys").get("log_utility_readJSONArray").replace("{path}", path));
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
        log.write(Locale.getInstance("sys").get("log_utility_saveJSONObject").replace("{path}", path));
        try (FileWriter file = new FileWriter(path)) {
            file.write(jo.toString(indent));
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveJSONArray(String path, JSONArray jo) {
        log.write(Locale.getInstance("sys").get("log_utility_saveJSONArray").replace("{path}", path));
        try (FileWriter file = new FileWriter(path)) {
            file.write(jo.toString(4));
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSHA256(String secret) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encodedhash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    public static String intToHex(int x) {
        return bytesToHex(ByteBuffer.allocate(4).putInt(x).array());
    }

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

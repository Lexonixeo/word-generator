package my.lexonix.wordgen.utility;

import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Utility {
    public static String toIntString(ArrayList<Integer> ints) {
        return ints.toString().replace("[", "").replace("]", "").replace(",", "");
    }

    public static void saveFile(String path, ArrayList<String> arr) {
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
            sb.append(" ");
        }
        return sb.toString();
    }

    public static JSONObject getJSONObject(String path) {
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

    public static void saveJSONObject(String path, JSONObject jo) {
        try (FileWriter file = new FileWriter(path)) {
            // Convert the JSONObject to a JSON string and write it
            file.write(jo.toString(1));
            // Alternatively, for pretty printing (if using org.json):
            // file.write(jsonObject.toString(4)); // Indent with 4 spaces

            file.flush(); // Ensure all data is written to the file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int countFiles(String path) {
        return Objects.requireNonNull(new File(path).list()).length;
    }
}

package my.lexonix.wordgen.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
}

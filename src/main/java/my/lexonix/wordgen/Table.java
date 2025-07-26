package my.lexonix.wordgen;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Table {
    private HashMap<Token, HashMap<Token, Integer>> table;
    private HashMap<Token, Integer> sumTable;
    private final String path;

    public Table() {
        table = new HashMap<>();
        sumTable = new HashMap<>();
        path = "table.txt";
    }

    public Table(String path) {
        table = readTable(path);
        sumTable = countSumTable(table);
        this.path = path;
    }

    public Token getRandomToken(Token before) {
        RandomCollection<Token> rc = new RandomCollection<>(new SecureRandom());
        for (Token t : table.get(before).keySet()) {
            rc.add(table.get(before).get(t), t);
        }
        return rc.next();
    }

    public void addPair(Token before, Token token) {
        if (!table.containsKey(before)) {
            table.put(before, new HashMap<>());
            sumTable.put(before, 0);
        }
        if (!table.get(before).containsKey(token)) {
            table.get(before).put(token, 0); // мб баг, надеюсь там хэшмап по ссылке
        }
        table.get(before).put(token, table.get(before).get(token) + 1);
        sumTable.put(before, sumTable.get(before) + 1);
    }

    public void saveTable() {
        ArrayList<String> strings = new ArrayList<>();
        for (Token firstToken : table.keySet()) {
            StringBuilder sb = new StringBuilder();
            ArrayList<Integer> ints = new ArrayList<>();

            sb.append(firstToken);
            sb.append(Letter.SEPARATOR);

            for (Token t : table.get(firstToken).keySet()) {
                sb.append(t);
                sb.append(Letter.SEPARATOR);
                ints.add(table.get(firstToken).get(t));
            }

            strings.add(sb.toString());
            strings.add(toIntString(ints));
        }
        saveFile(path, strings);
    }

    private static HashMap<Token, Integer> countSumTable(HashMap<Token, HashMap<Token, Integer>> table) {
        HashMap<Token, Integer> sumTable = new HashMap<>();
        for (Token firstToken : table.keySet()) {
            int sum = 0;

            for (Token t : table.get(firstToken).keySet()) {
                sum += table.get(firstToken).get(t);
            }

            sumTable.put(firstToken, sum);
        }
        return sumTable;
    }

    private static HashMap<Token, HashMap<Token, Integer>> readTable(String path) {
        HashMap<Token, HashMap<Token, Integer>> table = new HashMap<>();
        ArrayList<String> strings = readFile(path);
        for (int i = 0; i < strings.size(); i += 2) {
            String s = strings.get(i);
            ArrayList<Integer> ints = readIntArray(strings.get(i+1));
            ArrayList<Token> tokens = Letter.getLetters(s);
            Token firstToken = tokens.getFirst();
            table.put(firstToken, new HashMap<>());
            for (int j = 1; j < tokens.size(); j++) {
                table.get(firstToken).put(tokens.get(j), ints.get(j-1));
            }
        }
        return table;
    }

    private static String toIntString(ArrayList<Integer> ints) {
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

    private static ArrayList<Integer> readIntArray(String s) {
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
}


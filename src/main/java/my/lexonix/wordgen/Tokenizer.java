package my.lexonix.wordgen;

import java.util.ArrayList;

public class Tokenizer {
    private static final String LETTERS_ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюяABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~' 0123456789";
    public static final String WORDS_ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    public static final String SEPARATOR = "";

    public static ArrayList<Token> tokenize(String s) {
        return switch (Main.MODE) {
            case WORDS -> getWords(s);
            case LETTERS -> getLetters(s);
            case DOUBLE -> getDoubleLetters(s);
            case TRIPLE -> getTripleLetters(s);
        };
    }

    private static ArrayList<Token> getLetters(String sentence) {
        ArrayList<Token> letters = new ArrayList<>();
        for (char c : sentence.toCharArray()) {
            if (LETTERS_ALPHABET.contains(String.valueOf(c))) {
                letters.add(new Letter(c));
            }
        }
        return letters;
    }

    private static ArrayList<Token> getDoubleLetters(String sentence) {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Token> letters = getLetters(sentence);
        for (int i = 0; i < letters.size() - 1; i += 2) {
            tokens.add(new SimpleToken(letters.get(i).toString() + letters.get(i+1).toString()));
        }
        return tokens;
    }

    private static ArrayList<Token> getTripleLetters(String sentence) {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Token> letters = getLetters(sentence);
        for (int i = 0; i < letters.size() - 2; i += 3) {
            tokens.add(new SimpleToken(letters.get(i).toString() + letters.get(i+1).toString() + letters.get(i+2).toString()));
        }
        return tokens;
    }

    private static ArrayList<Token> getWords(String sentence) {
        ArrayList<Token> words = new ArrayList<>();
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            if (!WORDS_ALPHABET.contains(String.valueOf(sentence.charAt(i))) && !newWord.isEmpty()) {
                words.add(new Word(newWord.toString()));
                newWord.delete(0, newWord.length());
                continue;
            }
            if (!WORDS_ALPHABET.contains(String.valueOf(sentence.charAt(i)))) {
                continue;
            }
            newWord.append(sentence.charAt(i));
        }
        if (!newWord.isEmpty()) {
            words.add(new Word(newWord.toString()));
        }
        return words;
    }
}

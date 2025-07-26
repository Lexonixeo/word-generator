package my.lexonix.wordgen;

import java.util.ArrayList;

public class Word extends Token {
    public static final String SEPARATOR = " ";

    public Word(String word) {
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (!Main.ALPHABET.contains(String.valueOf(word.charAt(i)))) {
                break;
            }
            newWord.append(word.charAt(i));
        }
        super(newWord.toString().toLowerCase());
    }

    public static ArrayList<Token> getWords(String sentence) {
        ArrayList<Token> words = new ArrayList<>();
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            if (!Main.ALPHABET.contains(String.valueOf(sentence.charAt(i))) && !newWord.isEmpty()) {
                words.add(new Word(newWord.toString()));
                newWord.delete(0, newWord.length());
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

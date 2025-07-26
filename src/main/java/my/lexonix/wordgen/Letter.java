package my.lexonix.wordgen;

import java.util.ArrayList;

public class Letter extends Token {
    public static final String SEPARATOR = "";

    public Letter(String letter) {
        super(String.valueOf(letter.toLowerCase().charAt(0)));
    }

    public Letter(char letter) {
        super(String.valueOf(letter).toLowerCase());
    }

    public static ArrayList<Token> getLetters(String sentence) {
        ArrayList<Token> letters = new ArrayList<>();
        for (char c : sentence.toCharArray()) {
            if (Main.ALPHABET.contains(String.valueOf(c)) || Main.PUNCTUATION.contains(String.valueOf(c))) {
                letters.add(new Letter(c));
            }
        }
        return letters;
    }
}

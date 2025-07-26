package my.lexonix.wordgen;

public class Letter extends Token {
    public Letter(String letter) {
        super(String.valueOf(letter.toLowerCase().charAt(0)));
    }

    public Letter(char letter) {
        super(String.valueOf(letter).toLowerCase());
    }
}

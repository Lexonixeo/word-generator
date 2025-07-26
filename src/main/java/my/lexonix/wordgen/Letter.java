package my.lexonix.wordgen;

public class Letter extends Token {
    public Letter(char letter) {
        super(String.valueOf(letter).toLowerCase());
    }
}

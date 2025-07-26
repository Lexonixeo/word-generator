package my.lexonix.wordgen;

public class Word extends Token {
    public Word(String word) {
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (!Tokenizer.WORDS_ALPHABET.contains(String.valueOf(word.charAt(i)))) {
                break;
            }
            newWord.append(word.charAt(i));
        }
        super(newWord.toString().toLowerCase());
    }
}

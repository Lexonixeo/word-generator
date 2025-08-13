package my.lexonix.wordgen.tokens;

import my.lexonix.wordgen.utility.Logger;

import java.security.SecureRandom;
import java.util.ArrayList;

public class Tokenizer {
    // public static final String LETTERS_ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюяABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~' 0123456789";
    // public static final String WORDS_ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final String PUNCTUATION_ALPHABET = "'!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~' 0123456789";
    private static final int BOUND = 35;

    public static Token getLastToken(String s, TokenizerMode mode) {
        StringBuilder sr = new StringBuilder(s);
        sr.reverse();
        ArrayList<Token> rt = tokenize(sr.toString(), mode);
        StringBuilder firstTokenString = new StringBuilder(rt.getFirst().toString());
        return new Token(firstTokenString.reverse().toString());
    }

    public static ArrayList<Token> tokenize(String s, TokenizerMode mode) {
        Logger.write("[Tokenizer] Токенизация по моду " + mode);
        return switch (mode) {
            case WORDS -> getWords(s);
            case LETTERS -> getLetters(s);
            case DOUBLE -> getDoubleLetters(s);
            case TRIPLE -> getTripleLetters(s);
            case QUADRUPLE -> getQuadrupleLetters(s);
            case RANDOM -> getRandomTokens(s);
        };
    }
    
    public static String getSeparator(TokenizerMode mode) {
        return switch (mode) {
            case WORDS -> " ";
            case LETTERS, DOUBLE, TRIPLE, QUADRUPLE, RANDOM -> "";
        };
    }

    private static ArrayList<Token> getRandomTokens(String sentence) {
        ArrayList<Token> tokens = new ArrayList<>();
        SecureRandom r = new SecureRandom();
        StringBuilder lastToken = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            if ((lastToken.length() >= 2) && r.nextInt(100) >= BOUND) {
                tokens.add(new Token(lastToken.toString()));
                lastToken = new StringBuilder();
            }
            lastToken.append(sentence.charAt(i));
        }
        if (!lastToken.isEmpty()) {
            tokens.add(new Token(lastToken.toString()));
        }
        return tokens;
    }

    private static ArrayList<Token> getLetters(String sentence) {
        ArrayList<Token> letters = new ArrayList<>();
        for (char c : sentence.toCharArray()) {
            // if (LETTERS_ALPHABET.contains(String.valueOf(c))) {
                letters.add(new Token(String.valueOf(c)));
            // }
        }
        return letters;
    }

    private static ArrayList<Token> getDoubleLetters(String sentence) {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Token> letters = getLetters(sentence);
        for (int i = 0; i < letters.size() - 1; i += 2) {
            tokens.add(new Token(letters.get(i).toString() + letters.get(i+1).toString()));
        }
        return tokens;
    }

    private static ArrayList<Token> getTripleLetters(String sentence) {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Token> letters = getLetters(sentence);
        for (int i = 0; i < letters.size() - 2; i += 3) {
            tokens.add(new Token(letters.get(i).toString() + letters.get(i+1).toString() + letters.get(i+2).toString()));
        }
        return tokens;
    }

    private static ArrayList<Token> getQuadrupleLetters(String sentence) {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Token> letters = getLetters(sentence);
        for (int i = 0; i < letters.size() - 3; i += 4) {
            tokens.add(new Token(letters.get(i).toString() + letters.get(i+1).toString() + letters.get(i+2).toString() + letters.get(i+3).toString()));
        }
        return tokens;
    }

    private static ArrayList<Token> getWords(String sentence) {
        ArrayList<Token> words = new ArrayList<>();
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            if (Character.isWhitespace(sentence.charAt(i))) {
            // if (!LETTERS_ALPHABET.contains(String.valueOf(sentence.charAt(i))) || sentence.charAt(i) == ' ') {
                if (!newWord.isEmpty()) {
                    words.add(new Token(newWord.toString()));
                    newWord.delete(0, newWord.length());
                }
                continue;
            }
            newWord.append(sentence.charAt(i));
        }
        if (!newWord.isEmpty()) {
            words.add(new Token(newWord.toString()));
        }
        return words;
    }
}

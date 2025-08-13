package my.lexonix.wordgen.gateway;

import my.lexonix.wordgen.generator.WordGenerator;
import my.lexonix.wordgen.library.LibraryMode;
import my.lexonix.wordgen.library.WordLibrary;
import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.tokens.TokenizerMode;

import java.util.ArrayList;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class WordGateway {
    private final static long SIN_PERIOD = 1000 * 60 * 5 + 1000; // 5 минут 1 с

    public static long getCost(TokenizerMode mode, String word, String definition, Player p) {
        long cost;

        if (word != null && definition != null) {
            mode = TRIPLE;
            cost = 3;
        } else if (word != null) {
            cost = 8;
        } else if (definition != null) {
            cost = 6;
        } else {
            cost = 5;
        }

        cost = (long) (cost * (p.getWordsCount() + 10) / 10.0);
        cost = (long) (cost * (p.getBalance() + 100) / 100.0);

        if (word == null || definition == null) {
            switch (mode) {
                case LETTERS -> cost *= 1;
                case DOUBLE -> cost *= 1;
                case TRIPLE -> cost *= 1;
                case QUADRUPLE -> cost *= 2;
                case RANDOM -> cost *= 4;
            }
        }

        cost = (long) (cost * (1 + 0.5 * Math.sin(System.currentTimeMillis() * 2 * Math.PI / SIN_PERIOD)));

        return cost;
    }

    public static ArrayList<String> makeFourWordSentences(TokenizerMode mode, String word, String definition) {
        ArrayList<String> ans = new ArrayList<>();
        while (ans.size() < 4) {
            String wordSentence;
            if (word != null && definition == null) {
                synchronized (WordGenerator.getSynch(mode)) {
                    wordSentence = WordGenerator.makeWord(word, mode);
                }
            } else if (word != null) {
                wordSentence = word + " " + definition;
            } else if (definition == null) {
                synchronized (WordGenerator.getSynch(mode)) {
                    wordSentence = WordGenerator.makeWord(mode);
                }
            } else {
                String wordSentenceBef;
                synchronized (WordGenerator.getSynch(mode)) {
                    wordSentenceBef = WordGenerator.makeWord(mode);
                }
                String wordd = Tokenizer.tokenize(wordSentenceBef, WORDS).getFirst().toString();
                wordSentence = wordd + " " + definition;
            }
            if (!WordLibrary.isWordExists(WordLibrary.getWordie(wordSentence))) {
                ans.add(wordSentence);
            }
        }
        return ans;
    }

    public static void registerNewWord(Player p, String wordSentence, LibraryMode mode) {
        WordLibrary.addWord(wordSentence, mode, p.getPlayerID());
        p.addWord(WordLibrary.getWordie(wordSentence));
    }
}

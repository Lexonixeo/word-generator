package my.lexonix.wordgen.library;

import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.tokens.TokenizerMode;

import static my.lexonix.wordgen.tokens.TokenizerMode.TRIPLE;

public class Coster {
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
                case QUADRUPLE -> cost *= 2;
                case RANDOM -> cost *= 4;
            }
        }

        cost = (long) (cost * (1 + 0.5 * Math.sin(System.currentTimeMillis() * 2 * Math.PI / SIN_PERIOD)));

        return cost;
    }

    public static long getChangeDefCost(String word, Player p) {
        Word w = WordLibrary.getWord(word);
        return 100;
    }

    public static long getSendToBattleCost(String word, Player p) {
        Word w = WordLibrary.getWord(word);
        return 100;
    }
}

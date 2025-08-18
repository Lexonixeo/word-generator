package my.lexonix.wordgen.generator;

import my.lexonix.wordgen.tokens.Token;
import my.lexonix.wordgen.tokens.Tokenizer;
import my.lexonix.wordgen.utility.Locale;
import my.lexonix.wordgen.utility.Logger;
import my.lexonix.wordgen.utility.Utility;

import java.util.ArrayList;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class SentenceGenerator {
    private static final Logger log = new Logger("SentenceGenerator");

    public static void main() {
        Table table = new Table("data/tables/orfoep4.json", QUADRUPLE);
        //table.updateTable("data/texts/geom.txt");
        //table.updateTable("data/texts/ogephys.txt");
        //table.updateTable("data/texts/physcollege.txt");
        //table.updateTable("data/texts/ukrf.txt");
        //table.updateTable("data/texts/akrf.txt");
        //table.updateTable("data/texts/gkrf.txt");
        //table.updateTable("data/texts/crf.txt");
        //table.updateTable("data/texts/dal.txt");
        table.updateTable("data/texts/orfoep.txt");
        //table.updateTable("data/texts/wap.txt");
        //table.updateTable("data/texts/pin.txt");
        //table.updateTable("data/texts/jokes.txt");
        //table.updateTable("data/texts/history.txt");
        //table.updateTable("data/texts/predvybor.txt");
        table.saveTableJSON();
        SentenceGenerator.saveSentence(table, "data/sentence.txt", 500);
        //SentenceGenerator.saveSentence(table, "data/sentence.txt", 500, "ПРИКАЗНОВАТЬ");
    }

    public static String makeSentence(Table table, int tokensLength) {
        return makeSentence(table, tokensLength, table.getRandomFirstToken(), true);
    }

    public static String makeSentence(Table table, int tokensLength, String begin) {
        return begin + Tokenizer.getSeparator(table.getMode()) +
                makeSentence(table, tokensLength, Tokenizer.getLastToken(begin, table.getMode()), false);
    }

    public static String makeSentence(Table table, int tokensLength, Token firstToken, boolean showFirstToken) {
        log.write(Locale.getInstance("sys").get("log_sengen_make")
                .replace("{tokens}", String.valueOf(tokensLength))
                .replace("{mode}", table.getMode().toString()));
        StringBuilder sb = new StringBuilder();

        Token nextToken = firstToken;
        if (showFirstToken) {
            sb.append(nextToken);
            sb.append(Tokenizer.getSeparator(table.getMode()));
        }

        for (int i = 1; i < tokensLength; i++) {
            nextToken = table.getRandomToken(nextToken);
            sb.append(nextToken);
            sb.append(Tokenizer.getSeparator(table.getMode()));
        }

        return sb.toString();
    }

    public static void saveSentence(Table table, String path, int tokensLength) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength));
        Utility.saveFile(path, strings);
    }

    public static void saveSentence(Table table, String path, int tokensLength, String begin) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(makeSentence(table, tokensLength, begin));
        Utility.saveFile(path, strings);
    }
}

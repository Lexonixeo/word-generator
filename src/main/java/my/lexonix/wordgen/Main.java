package my.lexonix.wordgen;

import static my.lexonix.wordgen.TokenizerMode.*;

public class Main {
    public static final TokenizerMode MODE = TRIPLE;

    public static void main(String[] args) {
        Table table = new Table("data/tables/ukrf3.txt", true);
        // TableGenerator.updateTable(table, "data/texts/ukrf.txt");
        // table.saveTable();
        SentenceGenerator.makeSentence(table, "data/sentence.txt", 100);
    }
}
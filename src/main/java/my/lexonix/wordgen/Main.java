package my.lexonix.wordgen;

import my.lexonix.wordgen.generator.SentenceGenerator;
import my.lexonix.wordgen.generator.Table;

public class Main {
    public static void main(String[] args) {
        Table table = new Table("data/tables/ukrf3.txt");
        // table.updateTable("data/texts/funcmath.txt");
        // table.saveTable();
        SentenceGenerator.saveSentence(table, "data/sentence.txt", 1000, "cтатья ");
    }
}
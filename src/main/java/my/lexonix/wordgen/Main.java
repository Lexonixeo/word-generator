package my.lexonix.wordgen;

import my.lexonix.wordgen.generator.SentenceGenerator;
import my.lexonix.wordgen.generator.Table;

import static my.lexonix.wordgen.tokens.TokenizerMode.*;

public class Main {
    public static void main(String[] args) {
        Table table = new Table("data/tables/wapr.json", RANDOM);
        //table.updateTable("data/texts/geom.txt");
        //table.updateTable("data/texts/ogephys.txt");
        //table.updateTable("data/texts/physcollege.txt");
        //table.updateTable("data/texts/ukrf.txt");
        //table.updateTable("data/texts/akrf.txt");
        //table.updateTable("data/texts/gkrf.txt");
        //table.updateTable("data/texts/crf.txt");
        //table.updateTable("data/texts/dal.txt");
        table.updateTable("data/texts/wap.txt");
        //table.updateTable("data/texts/pin.txt");
        //table.updateTable("data/texts/jokes.txt");
        //table.updateTable("data/texts/history.txt");
        //table.updateTable("data/texts/predvybor.txt");
        table.saveTableJSON();
        SentenceGenerator.saveSentence(table, "data/sentence.txt", 500);
    }
}
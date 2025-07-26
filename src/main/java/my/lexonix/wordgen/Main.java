package my.lexonix.wordgen;

public class Main {
    public static void main(String[] args) {
        Table table = new Table("table.txt");
        //TableGenerator.updateTable(table, "text.txt");
        //table.saveTable();
        SentenceGenerator.makeSentence(table, "sentence.txt", 300);
    }
}
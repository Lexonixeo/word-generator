package my.lexonix.wordgen;

public class Main {
    public static final String ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final String PUNCTUATION = "'!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~' ";

    public static void main(String[] args) {
        Table table = new Table();

        TableGenerator.updateTable(table, "text.txt");

        table.saveTable();

        SentenceGenerator.makeSentence(table, "sentence.txt", new Letter("Ж"), 700);
    }
}
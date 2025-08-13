package my.lexonix.wordgen.library;

public class Word {
    private final String word;
    private String definition;
    private String ownerID;
    private long income;
    private long price;
    private long lastUpdate;

    public Word(String word, String definition, String ownerID) {
        this.word = word;
        this.definition = definition;
        this.ownerID = ownerID;
        this.income = 1;
        this.price = -1;
        this.lastUpdate = System.currentTimeMillis();
    }

    public Word(String word, String definition, String ownerID, long income, long price, long lastUpdate) {
        this.word = word;
        this.definition = definition;
        this.ownerID = ownerID;
        this.income = income;
        this.price = price;
        this.lastUpdate = lastUpdate;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public String getSentence() {
        return word + " " + definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate() {
        lastUpdate = System.currentTimeMillis();
    }
}

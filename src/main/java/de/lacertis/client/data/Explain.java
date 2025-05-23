package de.lacertis.client.data;

public class Explain {
    public String alias;
    public String author;
    public String message;

    public Explain() {
    }

    public Explain(String alias, String author, String message) {
        this.alias = alias;
        this.author = author;
        this.message = message;
    }
}
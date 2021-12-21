package ru.itis.protocols;

public enum Type {

    SELECT_PIXEL(""),
    STOP(""),
    START("");


    private final String title;

    Type(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

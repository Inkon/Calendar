package com.calendar.server.nlp;

import java.util.HashMap;

public abstract class AbstractFact {
    private HashMap<String, String> rawData = new HashMap<>();

    public AbstractFact() {

    }

    public AbstractFact(HashMap<String, String> rawData) {
        this.rawData = rawData;
    }

    public HashMap<String, String> getRawData() {
        return rawData;
    }

    public void setRawData(HashMap<String, String> rawData) {
        this.rawData = rawData;
    }

    public abstract Object analyze();
}

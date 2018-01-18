package edu.usc.cs_server.stockinfo.model;

/**
 * Created by Anuj Doiphode on 20-11-2017.
 */

public class CurrentStockDataModel {
    private String keyString;
    private String valueString;

    public CurrentStockDataModel(String keyString, String valueString) {
        this.keyString = keyString;
        this.valueString = valueString;
    }

    public String getKeyString() {
        return keyString;
    }

    public String getValueString() {
        return valueString;
    }
}

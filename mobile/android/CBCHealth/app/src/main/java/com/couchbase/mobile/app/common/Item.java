package com.couchbase.mobile.app.common;

public class Item {
    private String label;
    private String value;

    public Item() {
    }

    public Item(String key, String value) {
        this.label = key;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Item{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

package org.netbeans.cheat.sheet;
public class DisplayObject {
    private String key;
    private String value;
    private String description;
    public DisplayObject(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }
    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
    public String getDescription() {
        return description;
    }
}

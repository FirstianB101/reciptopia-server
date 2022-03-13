package kr.reciptopia.reciptopiaserver.helper;

import java.util.HashMap;
import java.util.Map;

public class Struct {

    private final Map<String, Object> values = new HashMap<>();

    public Struct() {
    }

    public Struct(Map<String, Object> values) {
        this.values.putAll(values);
    }

    @SuppressWarnings("unchecked")
    public <T> T valueOf(String key) {
        if (!values.containsKey(key))
            throw new IllegalArgumentException("No value exists for the key '" + key + "'");
        return (T) values.get(key);
    }

    public <T> Struct withValue(String key, T value) {
        Struct copied = new Struct(this.values);
        copied.values.put(key, value);
        return copied;
    }

}

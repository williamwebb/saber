package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

/**
 * Created by williamwebb on 7/5/15.
 */
public abstract class Preference<Type> {
    protected final SharedPreferences preferences;
    protected final String key;
    protected final Type defaultValue;

    public Preference(SharedPreferences preferences, String key) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue();
    }

    public Preference(SharedPreferences preferences, String key, String defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = fromString(defaultValue);
    }

    public abstract Type get();
    public abstract void set(Type type);
    protected abstract Type defaultValue();
    protected abstract Type fromString(String value);

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }
}


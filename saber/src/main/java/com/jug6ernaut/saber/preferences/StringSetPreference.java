package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class StringSetPreference extends Preference<Set<String>> {

    public StringSetPreference(SharedPreferences preferences, String key, Set<String> defaultValue) {
        super(preferences, key, defaultValue);
    }

    public Set<String> get() {
        return preferences.getStringSet(key, defaultValue);
    }

    @Override
    public void set(Set<String> value) {
        preferences.edit().putStringSet(key, value).apply();
    }

    @Override
    protected Set<String> defaultValue() {
        return new HashSet<>();
    }
}

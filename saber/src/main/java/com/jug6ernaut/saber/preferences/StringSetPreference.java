package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class StringSetPreference extends Preference<Set<String>> {

    private static Type typeToken = new TypeToken<HashSet<String>>(){}.getType();
    private static Gson gson = new Gson();

    public StringSetPreference(SharedPreferences preferences, String key) {
        super(preferences, key);
    }

    public StringSetPreference(SharedPreferences preferences, String key, String defaultValue) {
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

    @Override
    protected Set<String> fromString(String value) {
        return gson.fromJson(value,typeToken);
    }
}

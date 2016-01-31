package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class StringPreference extends Preference<String> {

    public StringPreference(SharedPreferences preferences, String key) {
        super(preferences, key);
    }

    public StringPreference(SharedPreferences preferences, String key, String defaultValue) {
        super(preferences, key, defaultValue);
    }

    public String get() {
        return preferences.getString(key, defaultValue);
    }

    @Override
    public void set(String value) {
        preferences.edit().putString(key, value).apply();
    }

    @Override
    protected String defaultValue() {
        return "";
    }

    @Override
    protected String fromString(String value) {
        return value;
    }
}

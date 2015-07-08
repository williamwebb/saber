package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class IntPreference extends Preference<Integer> {

    public IntPreference(SharedPreferences preferences, String key) {
        super(preferences, key);
    }

    public IntPreference(SharedPreferences preferences, String key, String defaultValue) {
        super(preferences, key, defaultValue);
    }

    public Integer get() {
        return preferences.getInt(key, defaultValue);
    }

    @Override
    public void set(Integer value) {
        preferences.edit().putInt(key, value).apply();
    }

    @Override
    protected Integer defaultValue() {
        return 0;
    }

    @Override
    protected Integer fromString(String value) {
        return Integer.valueOf(value);
    }
}

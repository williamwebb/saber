package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class BooleanPreference extends Preference<Boolean> {

    public BooleanPreference(SharedPreferences preferences, String key) {
        super(preferences, key);
    }

    public BooleanPreference(SharedPreferences preferences, String key, String defaultValue) {
        super(preferences, key, defaultValue);
    }

    public Boolean get() {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public void set(Boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    @Override
    protected Boolean defaultValue() {
        return false;
    }

    @Override
    protected Boolean fromString(String value) {
        if(value.length() == 0) return defaultValue();
        else return Boolean.valueOf(value);
    }
}

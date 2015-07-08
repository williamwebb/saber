package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

public class BooleanPreference extends Preference<Boolean> {

    public BooleanPreference(SharedPreferences preferences, String key, Boolean defaultValue) {
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
}

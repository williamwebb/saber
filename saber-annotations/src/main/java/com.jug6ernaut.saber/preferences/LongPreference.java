package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class LongPreference extends Preference<Long> {

    public LongPreference(SharedPreferences preferences, String key) {
        super(preferences, key);
    }

    public LongPreference(SharedPreferences preferences, String key, String defaultValue) {
        super(preferences, key, defaultValue);
    }

    public Long get() {
        return preferences.getLong(key, defaultValue);
    }

    @Override
    public void set(Long value) {
        preferences.edit().putLong(key, value).apply();
    }

    @Override
    protected Long defaultValue() {
        return 0L;
    }

    @Override protected Long fromString(String value) {
        if(value.length() == 0) return defaultValue();
        else return Long.valueOf(value);    }
}

package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

public class LongPreference extends Preference<Long> {

    public LongPreference(SharedPreferences preferences, String key, Long defaultValue) {
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
}

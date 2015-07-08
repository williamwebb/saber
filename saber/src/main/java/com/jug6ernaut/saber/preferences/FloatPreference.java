package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class FloatPreference extends Preference<Float> {

    public FloatPreference(SharedPreferences preferences, String key) {
        super(preferences, key);
    }

    public FloatPreference(SharedPreferences preferences, String key, String defaultValue) {
        super(preferences, key, defaultValue);
    }

    public Float get() {
        return preferences.getFloat(key, defaultValue);
    }

    @Override
    public void set(Float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    @Override
    protected Float defaultValue() {
        return 0F;
    }

    @Override
    protected Float fromString(String value) {
        return Float.valueOf(value);
    }
}

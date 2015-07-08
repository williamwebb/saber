package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;

public class FloatPreference extends Preference<Float> {

    public FloatPreference(SharedPreferences preferences, String key, Float defaultValue) {
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
}

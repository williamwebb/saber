package com.jug6ernaut.saber.preferences;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Map;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

/**
 * Created by williamwebb on 7/5/15.
 */
public abstract class Preference<Type> {
  protected final SharedPreferences      preferences;
  protected final String                 key;
  protected final Type                   defaultValue;
  private         OnChangeListener<Type> listener;

  public Preference(SharedPreferences preferences, String key) {
    this.preferences = preferences;
    this.key = key;
    this.defaultValue = defaultValue();
  }

  public Preference(SharedPreferences preferences, String key, String defaultValue) {
    this.preferences = preferences;
    this.key = key;
    this.defaultValue = fromString(defaultValue);
  }

  public abstract Type get();
  public abstract void set(Type type);
  protected abstract Type defaultValue();
  protected abstract Type fromString(String s);

  public boolean isSet() {
    return preferences.contains(key);
  }

  public void delete() {
    preferences.edit().remove(key).apply();
  }

  public String getKey() {
    return key;
  }

  public void setOnChangeListener(OnChangeListener<Type> listener) {
    this.listener = listener;
    preferences.registerOnSharedPreferenceChangeListener(spListener);
  }

  public interface OnChangeListener<Type> {
    void onChange(Type t);
  }

  public void unbind() {
    preferences.unregisterOnSharedPreferenceChangeListener(spListener);
  }

  private final OnSharedPreferenceChangeListener spListener = new OnSharedPreferenceChangeListener() {
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
      if (listener != null && key.equals(s)) {
        listener.onChange(get());
      }
    }
  };

  @Nullable
  public static Object getFrom(SharedPreferences sharedPreferences, String key) {
    Map<String, ?> data = sharedPreferences.getAll();
    if(data.containsKey(key)) {
      return data.get(key);
    } else {
      return null;
    }
  }
}


// Generated code from Saber. Do not modify!
package com.jug6ernaut.saber.example;

import com.jug6ernaut.saber.Saber.Finder;

import android.content.Context;

import com.jug6ernaut.saber.preferences.Preference;

public class MainActivity$$ExtraInjector {
  public static void inject(final Context context, final com.jug6ernaut.saber.example.MainActivity target) {
    Preference object;
    object = Finder.getPreference(context, "com.jug6ernaut.saber.example.MainActivity", "stringPreference", "wow", com.jug6ernaut.saber.preferences.StringPreference.class);
    if (object == null) {
      throw new IllegalStateException("Required extra with key 'stringPreference' for field 'stringPreference' was not found. If this extra is optional add '@Nullable' annotation.");
    }
    target.stringPreference = (com.jug6ernaut.saber.preferences.StringPreference) object;
    object = Finder.getPreference(context, "someFile", "someKey", "", com.jug6ernaut.saber.preferences.IntPreference.class);
    if (object == null) {
      throw new IllegalStateException("Required extra with key 'someKey' for field 'intPref' was not found. If this extra is optional add '@Nullable' annotation.");
    }
    target.intPref = (com.jug6ernaut.saber.preferences.IntPreference) object;
    object = Finder.getPreference(context, "com.jug6ernaut.saber.example.MainActivity", "boolPreference", "", com.jug6ernaut.saber.preferences.BooleanPreference.class);
    if (object == null) {
      throw new IllegalStateException("Required extra with key 'boolPreference' for field 'boolPreference' was not found. If this extra is optional add '@Nullable' annotation.");
    }
    target.boolPreference = (com.jug6ernaut.saber.preferences.BooleanPreference) object;
  }
}

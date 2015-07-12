//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jug6ernaut.saber.example;

import android.content.Context;
import com.jug6ernaut.saber.Saber.Finder;
import com.jug6ernaut.saber.example.MainActivity;
import com.jug6ernaut.saber.preferences.BooleanPreference;
import com.jug6ernaut.saber.preferences.IntPreference;
import com.jug6ernaut.saber.preferences.Preference;
import com.jug6ernaut.saber.preferences.StringPreference;

public class MainActivity$$ExtraInjector {
  public MainActivity$$ExtraInjector() {
  }

  public static void inject(Context context, MainActivity target) {
    Preference object = Finder.getPreference(context, "aFile", "stringPreference", "wow", StringPreference.class);
    if(object == null) {
      throw new IllegalStateException("Required extra with key \'stringPreference\' for field \'stringPreference\' was not found. If this extra is optional add \'@Nullable\' annotation.");
    } else {
      target.stringPreference = (StringPreference)object;
      object = Finder.getPreference(context, "someFile", "someKey", "", IntPreference.class);
      if(object == null) {
        throw new IllegalStateException("Required extra with key \'someKey\' for field \'intPref\' was not found. If this extra is optional add \'@Nullable\' annotation.");
      } else {
        target.intPref = (IntPreference)object;
        object = Finder.getPreference(context, "aFile", "boolPreference", "", BooleanPreference.class);
        if(object == null) {
          throw new IllegalStateException("Required extra with key \'boolPreference\' for field \'boolPreference\' was not found. If this extra is optional add \'@Nullable\' annotation.");
        } else {
          target.boolPreference = (BooleanPreference)object;
        }
      }
    }
  }
}

/*
 * Copyright 2013 Jake Wharton
 * Copyright 2014 Prateek Srivastava (@f2prateek)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jug6ernaut.saber.example;

import android.app.Activity;
import android.os.Bundle;
import com.jug6ernaut.saber.OnChange;
import com.jug6ernaut.saber.Preference;
import com.jug6ernaut.saber.PreferenceConfig;
import com.jug6ernaut.saber.Saber;
import com.jug6ernaut.saber.preferences.BooleanPreference;
import com.jug6ernaut.saber.preferences.IntPreference;
import com.jug6ernaut.saber.preferences.StringPreference;

@PreferenceConfig(file = "aFile") // file name applied to all sub @Preference
public class MainActivity extends Activity {

  @Preference(defaultValue = "wow") StringPreference stringPreference; // variable name is used as key
  @Preference(key = "someKey",file = "someFile") IntPreference intPref; // field level values always take precedence
  @Preference BooleanPreference boolPreference; // no information needed

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Saber.inject(this);

    int value = intPref.get();
    intPref.set(9999);

    String string = stringPreference.get();
    stringPreference.set("whatwhat");

    Boolean bool = boolPreference.get();
    boolPreference.set(true);

    intPref.delete();
    stringPreference.delete();
    boolPreference.delete();
  }

  @OnChange
  void onChange(String key) {
    System.err.println("onChange: " + key);
  }

  @OnChange(file = "someFile")
  void onChange2(String key) {
    System.err.println("onChange2: " + key);

  }

  @OnChange(file = "aFile")
  void onChange3(String key) {
    System.err.println("onChange3: " + key);


  }
}

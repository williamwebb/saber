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

import com.jug6ernaut.saber.Saber;
import com.jug6ernaut.saber.preferences.Preference;

import java.util.Set;

import saber.Bind;
import saber.OnChange;
import saber.PreferenceConfig;

@PreferenceConfig(file = "aFile") // file name applied to all sub @Preference
public class MainActivity extends Activity {

  @Bind(key = "another", dv = "[1,2,3]") Preference<Set<String>> stringPreference; // variable name is used as key
  @Bind(key = "someKey")             Preference<Integer>     intPref; // field level values always take precedence
  @Bind(key = "aKey")                Preference<Boolean>     boolPreference; // no information needed
//  @Bind(key = "2Key")                Preference<Object>      rawObjectObservable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Saber.inject(this);

    int value = intPref.get();
    intPref.set(9999);

    Set<String> string = stringPreference.get();
//    stringPreference.set("whatwhat");

    Boolean bool = boolPreference.get();
    boolPreference.set(true);

    intPref.delete();
    stringPreference.delete();
    boolPreference.delete();

    boolPreference.setOnChangeListener(new Preference.OnChangeListener<Boolean>() {
      @Override public void onChange(Boolean t) {

      }
    });
  }

  @OnChange(key = "aKey")
  void onChange(Boolean val) {
    System.err.println("onChange: " + val);
  }

  @OnChange(key = "someKey")
  void onChange2(Integer val) {
    System.err.println("onChange2: " + val);
  }
}

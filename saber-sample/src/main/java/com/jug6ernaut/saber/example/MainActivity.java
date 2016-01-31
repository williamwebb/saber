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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jug6ernaut.saber.Saber;
import com.jug6ernaut.saber.preferences.Preference;
import com.jug6ernaut.saber.preferences.StringSetPreference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import saber.Bind;
import saber.OnChange;

public class MainActivity extends Activity {

  @Bind(key = "another", dv = "[1,2,3]") Preference<Set<String>> stringPreference; // variable name is used as key
  @Bind(key = "someKey")                 Preference<Integer>     intPref; // field level values always take precedence
  @Bind(key = "someKey")                 Preference<Integer>     intPref1; // field level values always take precedence
  @Bind(key = "aKey", dv = "true")                    Preference<Boolean>     boolPreference; // no information needed
  @Bind(key = "2Key", file = "thisIsAFile")    StringSetPreference     rawObjectObservable;
  @Bind(key = "aKey2")                    Preference<Boolean>     boolPreference2; // no information needed

  Button button;
  TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Saber.bind(this);

    textView = (TextView) findViewById(R.id.text);
    button = (Button) findViewById(R.id.button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
//        Set<String> set = stringPreference.get();
        Set<String> set2 = new HashSet<String>();
        set2.add(System.currentTimeMillis()+"");
        stringPreference.set(set2);
      }
    });
    int value = intPref.get();
    intPref.set(9999);

    Set<String> string = stringPreference.get();

    boolPreference.setOnChangeListener(new Preference.OnChangeListener<Boolean>() {
      @Override public void onChange(Boolean t) {
        System.err.println("onChange: " + t);
      }
    });

    Boolean bool = boolPreference.get();
    boolPreference.set(true);

    intPref.delete();
    stringPreference.delete();
    boolPreference.delete();

    rawObjectObservable.set(Collections.singleton("wat2"));
    rawObjectObservable.delete();
  }

  @OnChange(key = "another")
  void onChangeAnother(Set<String> val) {
    Toast.makeText(this,"onChangeStringSet: " + val,Toast.LENGTH_LONG).show();
    System.err.println("onChangeStringSet: " + val);
  }

  @OnChange(key = "2Key", file = "aFileNotAPref")
  void onChangeStringSet(Set<String> val) {
    Toast.makeText(this,"onChangeStringSet: " + val,Toast.LENGTH_LONG).show();
    System.err.println("onChangeStringSet: " + val);
  }

  @OnChange(key = "aKey")
  void onChangeBoolean(Boolean val) {
    Toast.makeText(this,"onChangeBoolean: " + val,Toast.LENGTH_LONG).show();
    System.err.println("onChangeBoolean: " + val);
  }
}

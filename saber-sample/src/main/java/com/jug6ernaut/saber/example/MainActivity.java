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
import android.content.Intent;
import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.jug6ernaut.saber.Preference;
import com.jug6ernaut.saber.Saber;
import com.jug6ernaut.saber.preferences.IntPreference;

public class MainActivity extends Activity {

  @Preference(value="", defaultValue="", file="") IntPreference someIntPreference;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

//    Preference object = Saber.Finder.getExtra(this, "", "ExtraString", "", StringPreference.class);

    ButterKnife.inject(this);
    Saber.inject(this);

    System.err.println(someIntPreference.get());
    someIntPreference.set(1);
    System.err.println(someIntPreference.get());
    someIntPreference.set(2);
    System.err.println(someIntPreference.get());
    someIntPreference.set(10);
  }

  @OnClick(R.id.button) public void onLaunchButtonClick() {
    Intent intent = SampleActivity.getLaunchIntent(this, "a string", 4, ComplexParcelable.random(),
        new ExampleParcel("Andy"), "defaultKeyExtra");
    startActivity(intent);
  }
}

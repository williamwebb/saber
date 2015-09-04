Saber [![Build Status](https://travis-ci.org/jug6ernaut/saber.svg?branch=master)](https://travis-ci.org/jug6ernaut/saber)
============

Android SharedPreferences wrapper and injector. Based on Dart, which was originally based on ButterKnife.

SharedPreference "injection" library for Android which uses annotation processing to generate code that does direct field assignment of your SharedPreference.

Usage
=====
```
@PreferenceConfig(file = "someFile") // optional, file used for all sub @Preference unless override
public class MainActivity extends Activity {

  @Preference BooleanPreference boolPreference; // no information needed

  @Override protected void onCreate(Bundle savedInstanceState) {
    Saber.inject(this);

    Boolean bool = boolPreference.get();
    boolPreference.set(true);
  }
  
  @OnChange
  public void changeListener(String key) {
  	
  }
}
```
Saber provides wrapper classes for all shared preference applicable value types, type safe and null safe.

	IntPreference
	LongPreference
	FloatPreference
	BooleanPreference
	StringPreference
	StringSetPreference

Configuration
-------------

`@Preference` Has 3 fields, all optional

`value`  Key to be used, if not provided variable name is used.

`file` File to use, if not provided the default is used.

`defaultValue` Default value to be used, if not provided the class specific value will be used.

Download
--------

Download the latest JAR via Maven:


```xml
<dependency>
  <groupId>com.jug6ernaut</groupId>
  <artifactId>saber</artifactId>
  <version>0.5.0</version>
</dependency>
```

or Gradle:


```groovy
compile 'com.jug6ernaut:saber:0.5.0'
```


License
-------

    Copyright 2015 William Webb
    Copyright 2014 Prateek Srivastava (@f2prateek)
    Copyright 2013 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


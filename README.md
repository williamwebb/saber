[![Build Status](https://travis-ci.org/jug6ernaut/saber.svg?branch=master)](https://travis-ci.org/jug6ernaut/saber)

Saber
============

Android SharedPreferences wrapper and injector. Based on Dart, which was originally based on ButterKnife.

SharedPreference "injection" library for Android which uses annotation processing to generate code that does direct field assignment of your SharedPreference.

Usage
=====
```
@Preference(file = "aFile") // file name applied to all sub @Preference
public class MainActivity extends Activity {

  @Preference(defaultValue = "wow") StringPreference stringPreference; // variable name is used as key
  @Preference(value = "someKey",file = "someFile") IntPreference intPref; // field level values always take precedence
  @Preference BooleanPreference boolPreference; // no information needed

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Saber.inject(this);

    int value = intPref.get();
    intPref.set(9999);

    String string = stringPreference.get();
    stringPreference.set("whatwhat");
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
  <version>0.5.0-SNAPSHOT</version>
</dependency>
```

or Gradle:


```groovy
compile 'com.jug6eranut:saber:0.5.0-SNAPSHOT'
```


License
-------

    Copyright 2015 William Webb
    Copyright 2013 Jake Wharton
    Copyright 2014 Prateek Srivastava (@f2prateek)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


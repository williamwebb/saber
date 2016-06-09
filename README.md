Saber [![Build Status](https://travis-ci.org/williamwebb/saber.svg?branch=master)](https://travis-ci.org/williamwebb/saber)
============

Android SharedPreferences wrapper and injector. Based on Dart, which was originally based on ButterKnife.

SharedPreference "injection" library for Android which uses annotation processing to generate code that does direct field assignment of your SharedPreference..

Usage
=====
```
@PreferenceConfig(file = "someFile") // optional, file used for all sub @Preference unless override
public class MainActivity extends Activity {

  @Bind Preference<Boolean> boolPreference; // no information needed

  @Override protected void onCreate(Bundle savedInstanceState) {
    Saber.inject(this);

    Boolean bool = boolPreference.get();
    boolPreference.set(true);
  }
  
}
```
Saber provides wrapper classes for all shared preference applicable value types, preferenceType safe and null safe.

	IntPreference
	LongPreference
	FloatPreference
	BooleanPreference
	StringPreference
	StringSetPreference

Configuration
-------------

`@Bind` Has 3 fields, all optional

`key`  Key to be used, if not provided variable name is used.

`file` File to use, if not provided the default is used.

`dv` Default value to be used, if not provided the class specific value will be used.

Download
--------

Download the latest JAR via Maven:


```xml
<dependency>
  <groupId>com.jug6ernaut.saber</groupId>
  <artifactId>saber</artifactId>
  <version>0.6.0</version>
</dependency>
```

or Gradle:


```groovy
compile 'com.jug6ernaut.saber:saber:0.6.0'
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


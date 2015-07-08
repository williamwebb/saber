Saber
============

Android SharedPreferences wrapper and injector. Based on Dart, which was originally based on ButterKnife.

SharedPreference "injection" library for Android which uses annotation processing to generate code that does direct field assignment of your SharedPreference.

Usage
=====

	// only value/key is required
	@Preference(file="file", value="key", defaultValue="") IntPreference intPref;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Saber.inject(this);

		int value = intPref.get();
		intPref.set(9999);
	}

Saber provides wrapper classes for all shared preference applicable value types, type safe and null safe.

	IntPreference
	LongPreference
	FloatPreference
	BooleanPreference
	StringPreference
	StringSetPreference

Download
--------

Download [the latest JAR][1] or grab via Maven:

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


 [1]: http://jakewharton.github.io/butterknife/
 [2]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.f2prateek.dart&a=dart&v=LATEST

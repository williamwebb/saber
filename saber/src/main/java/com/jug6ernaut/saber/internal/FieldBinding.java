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

package com.jug6ernaut.saber.internal;

import javax.lang.model.type.TypeMirror;

final class FieldBinding implements Binding {

  private final String name;
  private final String file;
  private final String key;
  private final String defaultValue;
  private final TypeMirror type;

  FieldBinding(String name, String file, String key, String defaultValue, TypeMirror type) {
    this.name = name;
    this.file = file;
    this.key = key;
    this.defaultValue = defaultValue;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public TypeMirror getType() {
    return type;
  }

  @Override public String getDescription() {
    return "field '" + name + "'";
  }

  @Override public boolean isRequired() {
    return true;
  }

  public String getFile() {
    return file;
  }

  public String getKey() {
    return key;
  }

  public String getDefaultValue() {
    return defaultValue;
  }
}

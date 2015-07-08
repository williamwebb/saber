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
import java.util.*;

final class ExtraInjection {
  private final String file;
  private final String key;
  private final Object defaultValue;
  private final TypeMirror type;
  private final Set<FieldBinding> fieldBindings = new LinkedHashSet<>();

  ExtraInjection(String file,String key, Object defaultValue, TypeMirror type) {
    this.file = file;
    this.key = key;
    this.defaultValue = defaultValue;
    this.type = type;
  }

  public Collection<FieldBinding> getFieldBindings() {
    return fieldBindings;
  }

  public List<Binding> getRequiredBindings() {
    List<Binding> requiredBindings = new ArrayList<>();
    for (FieldBinding fieldBinding : fieldBindings) {
      if (fieldBinding.isRequired()) {
        requiredBindings.add(fieldBinding);
      }
    }
    return requiredBindings;
  }

  public void addFieldBinding(FieldBinding fieldBinding) {
    fieldBindings.add(fieldBinding);
  }

  public String getFile() {
    return file;
  }

  public String getKey() {
    return key;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public TypeMirror getType() {
    return type;
  }
}

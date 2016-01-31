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

import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.type.TypeMirror;

final class PreferenceInjector {
  private final Map<String, PreferenceInjection>    injectionMap = new LinkedHashMap<>();
  private final Map<String, Set<OnChangeInjection>> onChangeMap  = new LinkedHashMap<>(); // file/method name
  private final Set<String> sharedPrefs = new HashSet<>();
  private final String classPackage;
  private final String className;
  private final String targetClass;
  private String parentInjector;
  private String fileName;

  PreferenceInjector(String classPackage, String className, String targetClass) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
  }

  static void emitCast(StringBuilder builder, String fieldType) {
//    builder.append('(').append(getType(fieldType)).append(") ");
    builder.append('(').append(fieldType).append(") ");
  }

  static String getType(TypeMirror type) {
    if (type.getKind().isPrimitive()) {
      // Get wrapper for primitive types
      switch (type.getKind()) {
        case BOOLEAN:
          return "java.lang.Boolean";
        case BYTE:
          return "java.lang.Byte";
        case SHORT:
          return "java.lang.Short";
        case INT:
          return "java.lang.Integer";
        case LONG:
          return "java.lang.Long";
        case CHAR:
          return "java.lang.Character";
        case FLOAT:
          return "java.lang.Float";
        case DOUBLE:
          return "java.lang.Double";
        default:
          // Shouldn't happen
          throw new RuntimeException();
      }
    } else {
      return type.toString();
    }
  }

  static void emitHumanDescription(StringBuilder builder, List<Binding> bindings) {
    switch (bindings.size()) {
      case 1:
        builder.append(bindings.get(0).getDescription());
        break;
      case 2:
        builder.append(bindings.get(0).getDescription())
            .append(" and ")
            .append(bindings.get(1).getDescription());
        break;
      default:
        for (int i = 0, count = bindings.size(); i < count; i++) {
          Binding requiredField = bindings.get(i);
          if (i != 0) {
            builder.append(", ");
          }
          if (i == count - 1) {
            builder.append("and ");
          }
          builder.append(requiredField.getDescription());
        }
        break;
    }
  }

  void addField(String name, String file, String key, String defaultValue, String type) {
    getOrCreateExtraBinding(file,key,defaultValue,type).addFieldBinding(new FieldBinding(name, file, key, defaultValue, type));
  }

  void setParentInjector(String parentInjector) {
    this.parentInjector = parentInjector;
  }

  private PreferenceInjection getOrCreateExtraBinding(String file, String key, String defaultValue, String type) {
    PreferenceInjection preferenceInjection = injectionMap.get(file+key);
    if (preferenceInjection == null) {
      preferenceInjection = new PreferenceInjection(file,key,defaultValue,type);
      injectionMap.put(file+key, preferenceInjection);
    }
    return preferenceInjection;
  }

  String getFqcn() {
    return classPackage + "." + className;
  }

  String brewJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from Saber. Do not modify!\n");
    builder.append("package ").append(classPackage).append(";\n\n");
    builder.append("import com.jug6ernaut.saber.Saber.Finder;\n\n");
    builder.append("import android.content.Context;\n\n");
    builder.append("import com.jug6ernaut.saber.preferences.Preference;\n\n");
    builder.append("import com.google.gson.reflect.TypeToken;\n\n");
    builder.append("import android.content.SharedPreferences;\n\n");

    builder.append("@SuppressWarnings(\"unused\")\n");
    builder.append("public class ").append(className).append(" {\n");
    emitInject(builder);
    builder.append('\n');
    emitUnbind(builder);
    builder.append("}\n");
    return builder.toString();
  }


  //Context context, String file, String key, Object defaultValue, Class<Preference> preferenceType
  private void emitInject(StringBuilder builder) {
    builder.append("  public static void bind(")
        .append("final Context context, final ")
        .append(targetClass)
        .append(" target")
        .append(") {\n");

    // Local variable in which all extras will be temporarily stored.
    builder.append("    Preference object;\n");

    // Loop over each extras injection and emit it.
    String currentFile = "";
    for (PreferenceInjection injection : injectionMap.values()) {
      builder.append("\n");
      if(!currentFile.equals(injection.getFile())) {
        currentFile = injection.getFile();

        String sharedPreferenceName = injection.getFile().replace('.','_');
        if(!sharedPrefs.contains(sharedPreferenceName)) {
          builder.append("    final SharedPreferences sharedPreferences_").append(sharedPreferenceName).append(" = context.getSharedPreferences(\"").append(currentFile).append("\", Context.MODE_PRIVATE);\n");
          sharedPrefs.add(sharedPreferenceName);
        }
      }
      builder.append("    object = ");
      emitExtraInjection(builder, injection);
    }

    emitOnChangeBinding(builder, onChangeMap);

    builder.append("  }\n");
  }

  private void emitUnbind(StringBuilder builder) {
    builder.append("  public static void unbind(")
        .append("final ")
        .append(targetClass)
        .append(" target")
        .append(") {\n");

    for (PreferenceInjection injection : injectionMap.values()) {
      for(FieldBinding fieldBinding : injection.getFieldBindings()) {
        builder.append("    if(target.").append(fieldBinding.getName()).append(" != null) {\n");
        builder.append("      target.").append(fieldBinding.getName()).append(".unbind();\n");
        builder.append("      target.").append(fieldBinding.getName()).append(" = null;\n");
        builder.append("    }\n");
      }
    }

    builder.append("  }\n");
  }

  private void emitExtraInjection(StringBuilder builder, PreferenceInjection injection) {
    builder.append("new ")
        .append(injection.getType()).append("(sharedPreferences_").append(injection.getFile().replace('.','_')).append(",")
        .append("\"").append(injection.getKey()).append("\", ")
        .append("").append(new Gson().toJson(injection.getDefaultValue()))
        .append(");\n");

    List<Binding> requiredBindings = injection.getRequiredBindings();

    if (!requiredBindings.isEmpty()) {
      emitFieldBindings(builder, injection);
    }
  }

  private void emitFieldBindings(StringBuilder builder, PreferenceInjection injection) {
    Collection<FieldBinding> fieldBindings = injection.getFieldBindings();

    if (fieldBindings.isEmpty()) {
      return;
    }

    for (FieldBinding fieldBinding : fieldBindings) {
      builder.append("    target.").append(fieldBinding.getName()).append(" = ");
      builder.append("Finder.castPreference(object,\"")
          .append(injection.getFile())
          .append("\",\"")
          .append(injection.getKey())
          .append("\");\n");
    }
  }

  private void emitOnChangeBinding(StringBuilder builder, Map<String, Set<OnChangeInjection>> onChangeMap) {

    for (Map.Entry<String, Set<OnChangeInjection>> entry : onChangeMap.entrySet()) {
      String fileName = entry.getKey();
      Set<OnChangeInjection> keys = entry.getValue();

      String sharedPreferenceName = fileName.replace('.','_');
      if(!sharedPrefs.contains(sharedPreferenceName)) {
        builder.append("\n    final SharedPreferences sharedPreferences_").append(sharedPreferenceName).append(" = context.getSharedPreferences(\"").append(fileName).append("\", Context.MODE_PRIVATE);\n");
        sharedPrefs.add(sharedPreferenceName);
      }

      builder.append("\n    sharedPreferences_").append(sharedPreferenceName);
      builder.append(".registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {").append("\n");

      for (OnChangeInjection oci : keys) {
        String type = InjectPreferenceProcessor.REVERSE_TRANSLATION_TABLE.get(oci.preferenceType);
        String typeName = type.replace('.','_').replace("<","").replace(">","") + "_type";
        builder.append("      private TypeToken<").append(type).append("> ").append(typeName).append(" = new TypeToken<").append(type).append(">(){};\n");
      }

      builder.append("        @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {").append("\n");
      builder.append("          Object o = Preference.getFrom(sharedPreferences,key);\n");
      builder.append("          if(o != null)");
      builder.append("          switch( key ) {" ).append("\n");

      for (OnChangeInjection oci : keys) {
        String type = InjectPreferenceProcessor.REVERSE_TRANSLATION_TABLE.get(oci.preferenceType);
        String typeName = type.replace('.','_').replace("<","").replace(">","") + "_type";
          builder.append("            case \"").append(oci.key).append("\" :  ");
          builder.append("if(o.getClass().isAssignableFrom(").append(typeName).append(".getRawType())) ").append("target.").append(oci.methodName);
          builder.append("(");
          builder.append("(").append(type).append(")o");
          builder.append("); break;\n");
      }

      builder.append("          }").append("\n");
      builder.append("      }");
      builder.append("});").append("\n");
    }
  }

  public String getFileName() {
    return fileName;
  }

  public String getTargetClass() {
    return targetClass;
  }

  public Map<String, Set<OnChangeInjection>> getOnChangeMap() {
    return onChangeMap;
  }
}

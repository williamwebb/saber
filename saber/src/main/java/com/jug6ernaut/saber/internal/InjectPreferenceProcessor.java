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

import com.jug6ernaut.saber.OnChange;
import com.jug6ernaut.saber.Preference;
import com.jug6ernaut.saber.PreferenceConfig;
import com.jug6ernaut.saber.preferences.*;
import com.sun.tools.javac.code.Symbol;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

public final class InjectPreferenceProcessor extends AbstractProcessor {
  public static final String SUFFIX = "$$SaberInjector";

  List<String> CLASSES = Arrays.asList(
    BooleanPreference.class.getName(),
    FloatPreference.class.getName(),
    IntPreference.class.getName(),
    LongPreference.class.getName(),
    StringPreference.class.getName(),
    StringSetPreference.class.getName()
  );

  private Elements elementUtils;
  private Types typeUtils;
  private Filer filer;

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    elementUtils = env.getElementUtils();
    typeUtils = env.getTypeUtils();
    filer = env.getFiler();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> supportTypes = new LinkedHashSet<>();
    supportTypes.add(Preference.class.getCanonicalName());
    return supportTypes;
  }

  @Override public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
    Map<TypeElement, PreferenceInjector> targetClassMap = findAndParseTargets(env);

    for (Map.Entry<TypeElement, PreferenceInjector> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      PreferenceInjector preferenceInjector = entry.getValue();

      try {
        JavaFileObject jfo = filer.createSourceFile(preferenceInjector.getFqcn(), typeElement);
        Writer writer = jfo.openWriter();
        writer.write(preferenceInjector.brewJava());
        writer.flush();
        writer.close();
      } catch (IOException e) {
        error(typeElement, "Unable to write injector for type %s: %s", typeElement, e.getMessage());
      }
    }

    return true;
  }

  private Map<TypeElement, PreferenceInjector> findAndParseTargets(RoundEnvironment env) {
    Map<TypeElement, PreferenceInjector> targetClassMap = new LinkedHashMap<>();
    Set<TypeMirror> erasedTargetTypes = new LinkedHashSet<>();

    // Process each @Preference elements.
    for (Element element : env.getElementsAnnotatedWith(Preference.class)) {
      try {
        parsePreference(element, targetClassMap, erasedTargetTypes);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate extra injector for @Preference.\n\n%s",
            stackTrace.toString());
      }
    }

    for (Element element : env.getElementsAnnotatedWith(PreferenceConfig.class)) {
      try {
        parsePreferenceConfig(element,targetClassMap);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate extra injector for @Preference.\n\n%s",
                stackTrace.toString());
      }
    }

    for (Element element : env.getElementsAnnotatedWith(OnChange.class)) {
      try {
        parseOnChange(element,targetClassMap);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate extra injector for @Preference.\n\n%s",
                stackTrace.toString());
      }
    }

    // Try to find a parent injector for each injector.
    for (Map.Entry<TypeElement, PreferenceInjector> entry : targetClassMap.entrySet()) {
      String parentClassFqcn = findParentFqcn(entry.getKey(), erasedTargetTypes);
      if (parentClassFqcn != null) {
        entry.getValue().setParentInjector(parentClassFqcn + SUFFIX);
      }
    }

    return targetClassMap;
  }

  private boolean isValidForGeneratedCode(Class<? extends Annotation> annotationClass,
      String targetThing, Element element) {
    boolean hasError = false;
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify method modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
      error(element, "@%s %s must not be private or static. (%s.%s)",
          annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
          element.getSimpleName());
      hasError = true;
    }

    // Verify containing type.
    if (enclosingElement.getKind() != CLASS) {
      error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
          annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
          element.getSimpleName());
      hasError = true;
    }

    // Verify containing class visibility is not private.
    if (enclosingElement.getModifiers().contains(PRIVATE)) {
      error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
          annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
          element.getSimpleName());
      hasError = true;
    }

    // Verify its only applied to our specific classes
    String targetClassName = element.asType().toString();
    if (!CLASSES.contains(targetClassName)) {
      error(enclosingElement, "@%s may not be applied to %s."
              , annotationClass.getSimpleName()
              , element.asType().toString());
      hasError = true;
    }

    // Verify if extra exists its valid for type
    String defaultValue = element.getAnnotation(Preference.class).defaultValue();

    if(!isNullOrEmpty(defaultValue))
    if (!DefaultValueValidator.isValid(targetClassName, defaultValue)) {
      error(enclosingElement, "@%s has an invalid defaultValue %s."
              , annotationClass.getSimpleName()
              , defaultValue);
      hasError = true;
    }

    return hasError;
  }

  private void parseOnChange(Element element, Map<TypeElement, PreferenceInjector> targetClassMap) {
    String fileName = element.getAnnotation(OnChange.class).file();
    String targetName = element.getEnclosingElement().toString();

    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // check the PreferenceInjector has more then 2 instances of OnChange

    Symbol.MethodSymbol ms = (Symbol.MethodSymbol) element;
    // check method has 1 parameter
    // check method's 1 parameter is string

    com.sun.tools.javac.util.List<Symbol.VarSymbol> params = ms.getParameters();
    if(params.size() != 1){

    }
    if(params.get(0).asType().toString().equals("java.lang.String")) {

    }

    String methodName = ms.getSimpleName().toString();

    if(isNullOrEmpty(fileName)) fileName = targetName;

    boolean found = false;
    for (PreferenceInjector ei : targetClassMap.values()) {
      if(targetName.equals(ei.getTargetClass())) {
        Map<String, String> map = ei.getOnChangeMap();
        if(map.containsKey(fileName)) {
          error(element,"Why are you trying to have multiple listeners for the same SharedPreference in the same File?");
        }
        map.put(fileName,methodName);
        found = true;
      }
    }
    if(!found) {
      PreferenceInjector ei = getOrCreateTargetClass(targetClassMap,enclosingElement);
      ei.getOnChangeMap().put(fileName,methodName);
    }
  }

  private void parsePreferenceConfig(Element element, Map<TypeElement, PreferenceInjector> targetClassMap) {
    String fileName = element.getAnnotation(PreferenceConfig.class).file();
    String className = element.asType().toString();

    if(isNullOrEmpty(fileName)) error(element,"Class level requires a file name.");

    boolean found = false;
    for (PreferenceInjector ei : targetClassMap.values()) {
      if(className.equals(ei.getTargetClass())) {
        ei.setFileName(fileName);
        found = true;
      }
    }
    if(!found)error(element,"PreferenceConfig can only be applied to classes that contain @Preference.");
  }

  private void parsePreference(Element element, Map<TypeElement, PreferenceInjector> targetClassMap,
                               Set<TypeMirror> erasedTargetTypes) {
    boolean hasError = false;

    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    hasError |= isValidForGeneratedCode(Preference.class, "fields", element);

    if (hasError) {
      return;
    }

    // Assemble information on the injection point.
    String name = element.getSimpleName().toString();
    String file = element.getAnnotation(Preference.class).file();
    String key = element.getAnnotation(Preference.class).key();
    String defaultValue = element.getAnnotation(Preference.class).defaultValue();

    if(isNullOrEmpty(key)) key = name;
    if(isNullOrEmpty(key)) file = enclosingElement.asType().toString();

    TypeMirror type = element.asType();

    PreferenceInjector preferenceInjector = getOrCreateTargetClass(targetClassMap, enclosingElement);
    preferenceInjector.addField(name, file, key, defaultValue, type);

    // Add the type-erased version to the valid injection targets set.
    TypeMirror erasedTargetType = typeUtils.erasure(enclosingElement.asType());
    erasedTargetTypes.add(erasedTargetType);
  }

  private PreferenceInjector getOrCreateTargetClass(Map<TypeElement, PreferenceInjector> targetClassMap,
      TypeElement enclosingElement) {
    PreferenceInjector preferenceInjector = targetClassMap.get(enclosingElement);
    if (preferenceInjector == null) {
      String targetType = enclosingElement.getQualifiedName().toString();
      String classPackage = getPackageName(enclosingElement);
      String className = getClassName(enclosingElement, classPackage) + SUFFIX;

      preferenceInjector = new PreferenceInjector(classPackage, className, targetType);
      targetClassMap.put(enclosingElement, preferenceInjector);
    }
    return preferenceInjector;
  }

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  /** Finds the parent injector type in the supplied set, if any. */
  private String findParentFqcn(TypeElement typeElement, Set<TypeMirror> parents) {
    TypeMirror type;
    while (true) {
      type = typeElement.getSuperclass();
      if (type.getKind() == TypeKind.NONE) {
        return null;
      }
      typeElement = (TypeElement) ((DeclaredType) type).asElement();
      if (containsTypeMirror(parents, type)) {
        String packageName = getPackageName(typeElement);
        return packageName + "." + getClassName(typeElement, packageName);
      }
    }
  }

  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  public static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().length() == 0;
  }

  private boolean containsTypeMirror(Collection<TypeMirror> mirrors, TypeMirror query) {
    // Ensure we are checking against a type-erased version for normalization purposes.
    query = typeUtils.erasure(query);

    for (TypeMirror mirror : mirrors) {
      if (typeUtils.isSameType(mirror, query)) {
        return true;
      }
    }
    return false;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  private void error(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
  }

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }
}

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

import com.google.gson.reflect.TypeToken;
import com.jug6ernaut.saber.preferences.*;
import saber.Bind;
import saber.OnChange;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

public final class InjectPreferenceProcessor extends AbstractProcessor {
  public static final String SUFFIX = "$$SaberInjector";

  static final List<String> CLASSES = Arrays.asList(
    BooleanPreference.class.getName(),
    FloatPreference.class.getName(),
    IntPreference.class.getName(),
    LongPreference.class.getName(),
    StringPreference.class.getName(),
    StringSetPreference.class.getName()
  );

  static final List<String> TYPES = Arrays.asList(
    Boolean.class.getName(),
    Float.class.getName(),
    Integer.class.getName(),
    Long.class.getName(),
    String.class.getName(),
    new TypeToken<Set<String>>(){}.getType().toString()
  );

  static final Map<String, String> TRANSLATION_TABLE = new HashMap<>();
  static {
    TRANSLATION_TABLE.put(Boolean.class.getName(),BooleanPreference.class.getName());
    TRANSLATION_TABLE.put(Float.class.getName(),FloatPreference.class.getName());
    TRANSLATION_TABLE.put(Integer.class.getName(),IntPreference.class.getName());
    TRANSLATION_TABLE.put(Long.class.getName(),LongPreference.class.getName());
    TRANSLATION_TABLE.put(String.class.getName(),StringPreference.class.getName());
    TRANSLATION_TABLE.put(new TypeToken<Set<String>>(){}.getType().toString(),StringSetPreference.class.getName());
  }

  public static final Map<String, String> REVERSE_TRANSLATION_TABLE = new HashMap<>();
  static {
    REVERSE_TRANSLATION_TABLE.put(BooleanPreference.class.getName(),Boolean.class.getName());
    REVERSE_TRANSLATION_TABLE.put(FloatPreference.class.getName(),Float.class.getName());
    REVERSE_TRANSLATION_TABLE.put(IntPreference.class.getName(),Integer.class.getName());
    REVERSE_TRANSLATION_TABLE.put(LongPreference.class.getName(),Long.class.getName());
    REVERSE_TRANSLATION_TABLE.put(StringPreference.class.getName(),String.class.getName());
    REVERSE_TRANSLATION_TABLE.put(StringSetPreference.class.getName(),new TypeToken<Set<String>>(){}.getType().toString());
  }

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
    supportTypes.add(Bind.class.getCanonicalName());
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
        error(typeElement, "Unable to write injector for preferenceType %s: %s", typeElement, e.getMessage());
      }
    }

    return true;
  }

  private Map<TypeElement, PreferenceInjector> findAndParseTargets(RoundEnvironment env) {
    Map<TypeElement, PreferenceInjector> targetClassMap = new LinkedHashMap<>();
    Set<TypeMirror> erasedTargetTypes = new LinkedHashSet<>();
    Map<String,String> preferenceConfigMapping = new HashMap<>();

//    for (Element element : env.getElementsAnnotatedWith(PreferenceConfig.class)) {
//      try {
//        parsePreferenceConfig(element,preferenceConfigMapping);
//      } catch (Exception e) {
//        StringWriter stackTrace = new StringWriter();
//        e.printStackTrace(new PrintWriter(stackTrace));
//
//        error(element, "Unable to generate extra injector for @Preference.\n\n%s",
//            stackTrace.toString());
//      }
//    }

    // Process each @Bind elements.
    for (Element element : env.getElementsAnnotatedWith(Bind.class)) {
      try {
        parceBind(element, targetClassMap, erasedTargetTypes, preferenceConfigMapping);
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

    // Verify containing preferenceType.
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
    if (!CLASSES.contains(targetClassName) && !TYPES.contains(getGenericType(element.asType()).toString())) {
      error(enclosingElement, "@%s may not be applied to %s."
              , annotationClass.getSimpleName()
              , element.asType().toString());
      hasError = true;
    }

    // Verify if extra exists its valid for preferenceType
    String defaultValue = element.getAnnotation(Bind.class).dv();

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
    error(element,"@OnChange is not currently supported.");
    String key = element.getAnnotation(OnChange.class).key();
    String fileName = element.getAnnotation(OnChange.class).file();
    String targetName = element.getEnclosingElement().toString();

    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // check the PreferenceInjector has more then 2 instances of OnChange

    ExecutableElement ms = (ExecutableElement) element;
    // check method has 1 parameter
    // check method's 1 parameter is string

    String methodName = ms.getSimpleName().toString();
    List<? extends VariableElement> params = getMethodParams(element);
    if(params.size() != 1) error(element,"onChange can only take one parameter. " + element);

    String type = params.get(0).asType().toString();
    if (!TYPES.contains(type)) error(element, type + " is not supported. Must be of types : " + TYPES);
    if(!CLASSES.contains(type)) {
      type = TRANSLATION_TABLE.get(type);
    }
    if(isNullOrEmpty(fileName)) fileName = targetName;

    // TODO: need to pass file, methodName, and key
    // getSharedP...(file)...
    // switch..
    // case key: methodName( injectors.get(key).get() );
    OnChangeInjection injection = new OnChangeInjection(fileName,methodName,key, type);
    boolean found = false;
    for (PreferenceInjector ei : targetClassMap.values()) {
      if(targetName.equals(ei.getTargetClass())) {
        Map<String, Set<OnChangeInjection>> map = ei.getOnChangeMap();
        if(map.containsKey(fileName)) {
//          error(element,"Why are you trying to have multiple listeners for the same SharedPreference in the same File?");
          map.get(fileName).add(injection);
          found = true;
        }

      }
    }
    if(!found) {
      PreferenceInjector ei = getOrCreateTargetClass(targetClassMap,enclosingElement);
      ei.getOnChangeMap().put(fileName, new HashSet<OnChangeInjection>());
      ei.getOnChangeMap().get(fileName).add(injection);
    }
  }

//  private void parsePreferenceConfig(Element element, Map<String,String> targetClassMap) {
//    String fileName = element.getAnnotation(PreferenceConfig.class).file();
//    String className = element.asType().toString();
//
//    if(isNullOrEmpty(fileName)) error(element,"Class level requires a file name.");
//
//    targetClassMap.put(className,fileName);
//  }

  private void parceBind(Element element, Map<TypeElement, PreferenceInjector> targetClassMap,
                         Set<TypeMirror> erasedTargetTypes, Map<String, String> preferenceConfigMapping) {
    boolean hasError = false;

    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    hasError |= isValidForGeneratedCode(Bind.class, "fields", element);

    if (hasError) {
      return;
    }

    // Assemble information on the injection point.
    String name = element.getSimpleName().toString();
    String file = element.getAnnotation(Bind.class).file();
    String key = element.getAnnotation(Bind.class).key();
    String defaultValue = element.getAnnotation(Bind.class).dv();

    if(isNullOrEmpty(key)) key = name;

    String  type = element.asType().toString();
    if(!CLASSES.contains(type)) {
      type = TRANSLATION_TABLE.get(getGenericType(element.asType()).toString());
    }

    PreferenceInjector preferenceInjector = getOrCreateTargetClass(targetClassMap, enclosingElement);

    String fileName = preferenceInjector.getFileName();
    String targetClass = preferenceInjector.getTargetClass();

    if(isNullOrEmpty(file)) {
      if (!isNullOrEmpty(fileName)) {
        file = fileName; // class level
      } else if(preferenceConfigMapping.containsKey(targetClass)) {
        file = preferenceConfigMapping.get(targetClass);
      } else {
        file = targetClass; // fall back to targetClass
      }
    }

    preferenceInjector.addField(name, file, key, defaultValue, type);

    // Add the preferenceType-erased version to the valid injection targets set.
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

  /** Finds the parent injector preferenceType in the supplied set, if any. */
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
    // Ensure we are checking against a preferenceType-erased version for normalization purposes.
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

  public static TypeMirror getGenericType(final TypeMirror type) {
    final TypeMirror[] result = { null };

    type.accept(new SimpleTypeVisitor6<Void, Void>()
    {
      @Override
      public Void visitDeclared(DeclaredType declaredType, Void v)
      {
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty())
        {
          result[0] = typeArguments.get(0);
        }
        return null;
      }
      @Override
      public Void visitPrimitive(PrimitiveType primitiveType, Void v)
      {
        return null;
      }
      @Override
      public Void visitArray(ArrayType arrayType, Void v)
      {
        return null;
      }
      @Override
      public Void visitTypeVariable(TypeVariable typeVariable, Void v)
      {
        return null;
      }
      @Override
      public Void visitError(ErrorType errorType, Void v)
      {
        return null;
      }
      @Override
      protected Void defaultAction(TypeMirror typeMirror, Void v)
      {
        throw new UnsupportedOperationException();
      }
    }, null);

    return result[0];
  }

  public static List<? extends VariableElement> getMethodParams(Element element) {

    // This should be guarded by the annotation's @Target but it's worth a check for safe casting.
    if (!(element instanceof ExecutableElement) || element.getKind() != METHOD) {
      throw new IllegalStateException(
          String.format("@%s annotation must be on a method.",""));
    }

    ExecutableElement executableElement = (ExecutableElement) element;

    return executableElement.getParameters();
  }
}

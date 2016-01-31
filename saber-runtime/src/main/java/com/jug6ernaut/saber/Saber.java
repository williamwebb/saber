package com.jug6ernaut.saber;

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

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jug6ernaut.saber.preferences.Preference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import saber.Bind;

public class Saber {
  static final Map<Class<?>, Method> INJECTORS = new LinkedHashMap<Class<?>, Method>();
  static final Method NO_OP = null;
  private static final String TAG = "Saber";
  private static boolean debug = false;

  private Saber() {
    // No instances.
  }

  /** Control whether debug logging is enabled. */
  public static void setDebug(boolean debug) {
    Saber.debug = debug;
  }

  /**
   * Inject fields annotated with {@link Bind} in the specified {@link
   * android.app.Activity}.
   * The intent that called this activity will be used as the source of the extras bundle.
   *
   * @param target Target activity for field injection.
   * @throws Saber.UnableToInjectException if injection could not be
   * performed.
   * @see android.content.Intent#getExtras()
   */
  public static void bind(Activity target) {
    bind(target, target);
  }

  public static void bind(Application target) {
    bind(target, target);
  }

  /**
   * Inject fields annotated with {@link Bind} in the specified {@link
   * android.app.Fragment}.
   * The arguments that this fragment was called with will be used as the source of the extras
   * bundle.
   *
   * @param target Target fragment for field injection.
   * @throws Saber.UnableToInjectException if injection could not be
   * performed.
   * @see android.app.Fragment#getArguments()
   */
  public static void bind(Fragment target) {
    bind(target, target.getActivity());
  }

  public static void bind(Object target, Context source) {
    Class<?> targetClass = target.getClass();
    try {
      if (debug) Log.d(TAG, "Looking up extra injector for " + targetClass.getName());
      Method inject = findInjectorForClass(targetClass);
      if (inject != null) {
        inject.invoke(null, source, target);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new UnableToInjectException("Unable to inject extras for " + target, e);
    }
  }

  public static void unbind(Object target) {
    Class<?> cls = target.getClass();
    String clsName = cls.getName();

    try {
      Class<?> injector = Class.forName(clsName + Bind.SUFFIX);
      injector.getMethod("unbind").invoke(null);
    } catch (Exception ignored) { }
  }

  private static Method findInjectorForClass(Class<?> cls) throws NoSuchMethodException {
    Method inject = INJECTORS.get(cls);
    if (inject != null) {
      if (debug) Log.d(TAG, "HIT: Cached in injector map.");
      return inject;
    }
    String clsName = cls.getName();
    if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
      if (debug) Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
      return NO_OP;
    }
    try {
      Class<?> injector = Class.forName(clsName + Bind.SUFFIX);
      inject = injector.getMethod("bind", Context.class, cls);
      if (debug) Log.d(TAG, "HIT: Class loaded injection class.");
    } catch (ClassNotFoundException e) {
      if (debug) Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
      inject = findInjectorForClass(cls.getSuperclass());
    }
    INJECTORS.put(cls, inject);
    return inject;
  }

  /** Simpler version of {@link android.os.Bundle#get(String)} which infers the target type. */
  @SuppressWarnings({ "unchecked", "UnusedDeclaration" })
  // Checked by runtime cast. Public API.
  public static <T> T get(Bundle bundle, String key) {
    return (T) bundle.get(key);
  }

  /**
   * A means of finding an extra in either an {@link android.app.Activity}, {@link
   * android.app.Fragment} or a {@link android.os.Bundle}. Exposed for use only
   * by generated code.
   * If any of the means to get a bundle are null, this will simply return a null.
   */
  public static class Finder {
//    public static <P extends Preference> P getPreference(Context context, String file, String key, String defaultValue, Class<P> type) {
//      SharedPreferences prefs;
//      if(isNullOrEmpty(file)) prefs = PreferenceManager.getDefaultSharedPreferences(context);
//      else prefs = context.getSharedPreferences(file,Context.MODE_PRIVATE);
//
//      try {
//        if(isNullOrEmpty(defaultValue))
//          return type.getDeclaredConstructor(SharedPreferences.class,String.class).newInstance(prefs,key);
//        else
//          return type.getDeclaredConstructor(SharedPreferences.class,String.class,String.class).newInstance(prefs,key,defaultValue);
//      } catch (Exception e) {
//        throw new IllegalStateException("");
//      }
//    }
    public static <T> T castPreference(Preference preference, String file, String key) {
      try {
        return (T) preference;
      } catch (ClassCastException e) {
        throw new IllegalStateException("Preference '"
            + "File: "
            + file
            + " Key: "
            + key
            + " could not be set!");
      }
    }
  }

  public static class UnableToInjectException extends RuntimeException {
    UnableToInjectException(String message, Throwable cause) {
      super(message, cause);
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

}

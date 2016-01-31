package com.jug6ernaut.saber.internal;

/**
 * Created by itk7799 on 12/22/15.
 */
public class OnChangeInjection {
  public final String fileName;
  public final String methodName;
  public final String key;
  public final String preferenceType;
//  public final String type;

  public OnChangeInjection(String fileName, String methodName, String key, String preferenceType) {
    this.fileName = fileName;
    this.methodName = methodName;
    this.key = key;
    this.preferenceType = preferenceType;
  }
}

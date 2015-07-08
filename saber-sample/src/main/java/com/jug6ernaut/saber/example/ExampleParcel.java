package com.jug6ernaut.saber.example;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class ExampleParcel {

  String name;

  @ParcelConstructor
  public ExampleParcel(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

package com.jug6ernaut.saber;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by williamwebb on 7/11/15.
 */
@Retention(CLASS) @Target(TYPE) public @interface PreferenceConfig {
    String file();
}

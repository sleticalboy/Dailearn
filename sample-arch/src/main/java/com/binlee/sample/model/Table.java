package com.binlee.sample.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created on 21-2-26.
 *
 * @author binlee sleticalboy@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
  String name();

  @Retention(RetentionPolicy.RUNTIME) @interface Column {
    String name();

    String type();

    String defVal() default "";

    boolean indexed() default false;

    boolean unique() default false;
  }
}

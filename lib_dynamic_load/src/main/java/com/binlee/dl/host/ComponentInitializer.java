package com.binlee.dl.host;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
class ComponentInitializer {

  private ComponentInitializer() {
    //no instance
  }

  @SuppressWarnings("unchecked")
  static <T> T initialize(ClassLoader loader, String className) {
    try {
      return (T) loader.loadClass(className).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      return null;
    }
  }
}

package com.binlee.dl.host.proxy;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
final class PuppetFactory {

  private PuppetFactory() {
    //no instance
  }

  @SuppressWarnings("unchecked")
  static <T> T load(ClassLoader loader, String className) {
    try {
      return (T) loader.loadClass(className).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      return null;
    }
  }
}

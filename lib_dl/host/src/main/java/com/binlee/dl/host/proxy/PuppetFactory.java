package com.binlee.dl.host.proxy;

import com.binlee.dl.host.DlManager;

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
  static <T> T create(String className) {
    try {
      final Class<?> clazz = DlManager.loadClass(className);
      return clazz == null ? null : (T) clazz.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      return null;
    }
  }
}

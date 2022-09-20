package com.binlee.dl.host.proxy;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
public interface IMaster {
  // 通过 intent 拿到 target component，之后通过反射初始化并注入参数
  // 然后通过此类代理插件类的行为
  String TARGET_COMPONENT = "com.binlee.dl.TARGET_COMPONENT";
}

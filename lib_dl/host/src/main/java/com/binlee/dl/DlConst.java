package com.binlee.dl;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
public interface DlConst {

  // 通过 intent 拿到 target component，之后通过反射初始化并注入参数
  // 然后通过此类代理插件类的行为
  String REAL_COMPONENT = "com.binlee.dl.REAL_COMPONENT";
}

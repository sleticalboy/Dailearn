package com.binlee.apm.jvmti;

/**
 * Created on 2022/7/18
 *
 * @author binlee
 */
public final class JvmtiConfig {

  /** 文件存放目录 */
  public final String rootDir;
  /** 代理 so 文件绝对路径 */
  public String agentLib;

  /** 对象分配 */
  public boolean objectAlloc = false;
  /** 对象释放 */
  public boolean objectFree = false;

  /** 异常创建 */
  public boolean exceptionCreate = false;
  /** 异常捕获 */
  public boolean exceptionCatch = false;

  /** 方法进入 */
  public boolean methodEnter = false;
  /** 方法退出 */
  public boolean methodExit = false;

  public JvmtiConfig(String rootDir) {
    this.rootDir = rootDir;
  }

  /**
   * 转成 jvm 可识别的选项，注意选项中不能带 '='!
   *
   * @return {@link String}
   */
  public String toOptions() {
    return String.format(
      "root_dir:%s;obj_alloc:%s;obj_free:%s;ex_create:%s;ex_catch:%s;method_enter:%s;method_exit:%s;",
      rootDir, objectAlloc, objectFree, exceptionCreate, exceptionCatch, methodEnter, methodExit);
  }

  @Override public String toString() {
    return toOptions();
  }
}

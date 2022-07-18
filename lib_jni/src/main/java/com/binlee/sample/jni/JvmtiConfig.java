package com.binlee.sample.jni;

/**
 * Created on 2022/7/18
 *
 * @author binlee
 */
public final class JvmtiConfig {

  /** 文件存放目录 */
  public final String rootDir;
  /** 代理 so 文件绝对路径 */
  public String agentFile;

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
}

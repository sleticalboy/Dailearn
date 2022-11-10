package com.example.freevideo;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public enum DyState {

  /** 未下载 */ NONE,
  /** 正在下载 */ DOWNLOADING,
  /** 已下载 */ DOWNLOADED,
  /** 无法下载 */ BROKEN
}

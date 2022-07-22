package com.sleticalboy.learning.encryption

class SM4Context {

  /**
   * 加密模式
   */
  private var mode = 1
  private var sk: LongArray
  private var isPadding = true

  fun getMode(): Int = mode

  fun setMode(mode: Int) {
    this.mode = mode
  }

  fun getSk(): LongArray = sk

  fun setSk(sk: LongArray) {
    this.sk = sk
  }

  fun isPadding(): Boolean = isPadding

  fun setPadding(padding: Boolean) {
    isPadding = padding
  }

  init {
    sk = LongArray(32)
  }
}
package com.binlee.learning.encryption

import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * SM4 分组加密算法
 */
class SM4 {

  private fun GET_ULONG_BE(b: ByteArray, i: Int): Long {
    return ((b[i].toInt() and 0xff).toLong() shl 24 or (b[i + 1].toInt() and 0xff shl 16).toLong()
        or (b[i + 2].toInt() and 0xff shl 8).toLong()
        or ((b[i + 3].toInt() and 0xff).toLong() and 0xffffffffL))
  }

  private fun PUT_ULONG_BE(n: Long, b: ByteArray, i: Int) {
    b[i] = (0xFF and n.toInt() shr 24).toByte()
    b[i + 1] = (0xFF and n.toInt() shr 16).toByte()
    b[i + 2] = (0xFF and n.toInt() shr 8).toByte()
    b[i + 3] = (0xFF and n.toInt()).toByte()
  }

  /**
   * 左移指令
   *
   * @param x 数据
   * @param n 移动位数
   * @return 结果
   */
  private fun SHL(x: Long, n: Int): Long {
    return x and -0x1 shl n
  }

  private fun ROTL(x: Long, n: Int): Long {
    return SHL(x, n) or x shr 32 - n
  }

  /**
   * 两个数交换位置
   */
  private fun swap(sk: LongArray?, i: Int) {
    val t = sk!![i]
    sk[i] = sk[31 - i]
    sk[31 - i] = t
  }

  private fun sm4Sbox(inch: Byte): Byte {
    val i: Int = inch.toInt() and 0xFF
    return SboxTable[i]
  }

  private fun sm4Lt(ka: Long): Long {
    var bb = 0L
    var c = 0L
    val a = ByteArray(4)
    val b = ByteArray(4)
    PUT_ULONG_BE(ka, a, 0)
    b[0] = sm4Sbox(a[0])
    b[1] = sm4Sbox(a[1])
    b[2] = sm4Sbox(a[2])
    b[3] = sm4Sbox(a[3])
    bb = GET_ULONG_BE(b, 0)
    c = bb xor ROTL(bb, 2) xor ROTL(bb, 10) xor ROTL(bb, 18) xor ROTL(bb, 24)
    return c
  }

  private fun sm4F(x0: Long, x1: Long, x2: Long, x3: Long, rk: Long): Long {
    return x0 xor sm4Lt(x1 xor x2 xor x3 xor rk)
  }

  private fun sm4CalciRK(ka: Long): Long {
    var bb = 0L
    var rk = 0L
    val a = ByteArray(4)
    val b = ByteArray(4)
    PUT_ULONG_BE(ka, a, 0)
    b[0] = sm4Sbox(a[0])
    b[1] = sm4Sbox(a[1])
    b[2] = sm4Sbox(a[2])
    b[3] = sm4Sbox(a[3])
    bb = GET_ULONG_BE(b, 0)
    rk = bb xor ROTL(bb, 13) xor ROTL(bb, 23)
    return rk
  }

  private fun sm4_setKey(SK: LongArray?, key: ByteArray) {
    val MK = LongArray(4)
    val k = LongArray(36)
    var i = 0
    MK[0] = GET_ULONG_BE(key, 0)
    MK[1] = GET_ULONG_BE(key, 4)
    MK[2] = GET_ULONG_BE(key, 8)
    MK[3] = GET_ULONG_BE(key, 12)
    k[0] = MK[0] xor FK[0].toLong()
    k[1] = MK[1] xor FK[1].toLong()
    k[2] = MK[2] xor FK[2].toLong()
    k[3] = MK[3] xor FK[3].toLong()
    while (i < 32) {
      k[i + 4] = k[i] xor sm4CalciRK(k[i + 1] xor k[i + 2] xor k[i + 3] xor CK[i].toLong())
      SK!![i] = k[i + 4]
      i++
    }
  }

  private fun sm4_one_round(sk: LongArray?, input: ByteArray, output: ByteArray) {
    var i = 0
    val ulbuf = LongArray(36)
    ulbuf[0] = GET_ULONG_BE(input, 0)
    ulbuf[1] = GET_ULONG_BE(input, 4)
    ulbuf[2] = GET_ULONG_BE(input, 8)
    ulbuf[3] = GET_ULONG_BE(input, 12)
    while (i < 32) {
      ulbuf[i + 4] = sm4F(ulbuf[i], ulbuf[i + 1], ulbuf[i + 2], ulbuf[i + 3], sk!![i])
      i++
    }
    PUT_ULONG_BE(ulbuf[35], output, 0)
    PUT_ULONG_BE(ulbuf[34], output, 4)
    PUT_ULONG_BE(ulbuf[33], output, 8)
    PUT_ULONG_BE(ulbuf[32], output, 12)
  }

  // 扩展/还原数组
  private fun padding(input: ByteArray?, mode: Int): ByteArray? {
    if (input == null) {
      return null
    }
    val ret: ByteArray
    if (mode == SM4_ENCRYPT) {
      Log.d("SM4", "加密-->扩展数组长度")
      val p = 16 - input.size % 16 // 16 - 余数 = 扩展的长度
      ret = ByteArray(input.size + p) // 扩展数组的长度 = input 的长度 + 扩展的长度
      // 将 input 拷贝给 ret
      System.arraycopy(input, 0, ret, 0, input.size)
      for (i in 0 until p) {
        ret[input.size + i] = p.toByte() // 将多出来的位置写满 p
      }
    } else {
      Log.d("SM4", "解密-->还原数组")
      // 取 input 最后一个元素, 即加密时的扩展长度
      val p = input[input.size - 1].toInt()
      Log.d("SM4", "p:$p")
      Log.d("SM4", "input.length:" + input.size)
      ret = ByteArray(input.size - p)
      System.arraycopy(input, 0, ret, 0, input.size - p)
    }
    return ret
  }

  /**
   * 设置加密秘钥
   *
   * @param ctx
   * @param key
   * @throws Exception
   */
  @Throws(Exception::class)
  fun setEncryptKey(ctx: SM4Context?, key: ByteArray?) {
    if (ctx == null) {
      throw Exception("ctx is null!")
    }
    if (key == null || key.size != 16) {
      throw Exception("key error!")
    }
    ctx.setMode(SM4_ENCRYPT)
    sm4_setKey(ctx.getSk(), key)
  }

  /**
   * 设置解密秘钥
   *
   * @param ctx
   * @param key
   * @throws Exception
   */
  @Throws(Exception::class)
  fun setDecryptKey(ctx: SM4Context?, key: ByteArray?) {
    if (ctx == null) {
      throw Exception("ctx is null!")
    }
    if (key == null || key.size != 16) {
      throw Exception("key error!")
    }
    var i = 0
    ctx.setMode(SM4_DECRYPT)
    sm4_setKey(ctx.getSk(), key)
    i = 0
    while (i < 16) {
      swap(ctx.getSk(), i)
      i++
    }
  }

  /**
   * ecb 模式加密
   *
   * @param ctx
   * @param input
   * @return
   * @throws Exception
   */
  @Throws(Exception::class)
  fun cryptECB(ctx: SM4Context, input: ByteArray?): ByteArray? {
    var input: ByteArray? = input ?: throw Exception("input is null!")
    if (ctx.isPadding() && ctx.getMode() == SM4_ENCRYPT) {
      input = padding(input, SM4_ENCRYPT)
    }
    var length = input!!.size
    val bins = ByteArrayInputStream(input)
    val bous = ByteArrayOutputStream()
    while (length > 0) {
      val `in` = ByteArray(16)
      val out = ByteArray(16)
      bins.read(`in`)
      sm4_one_round(ctx.getSk(), `in`, out)
      bous.write(out)
      length -= 16
    }
    var output = bous.toByteArray()
    if (ctx.isPadding() && ctx.getMode() == SM4_DECRYPT) {
      output = padding(output, SM4_DECRYPT)
    }
    bins.close()
    bous.close()
    return output
  }

  /**
   * cbc 模式加密
   *
   * @param ctx
   * @param iv
   * @param input
   * @return
   * @throws Exception
   */
  @Throws(Exception::class)
  fun cryptCBC(ctx: SM4Context, iv: ByteArray?, input: ByteArray?): ByteArray? {
    var buf = input
    if (iv == null || iv.size != 16) {
      throw Exception("iv error!")
    }
    if (buf == null) {
      throw Exception("input is null!")
    }
    if (ctx.isPadding() && ctx.getMode() == SM4_ENCRYPT) {
      buf = padding(buf, SM4_ENCRYPT)
    }
    var i = 0
    var length = buf!!.size
    val bins = ByteArrayInputStream(buf)
    val bous = ByteArrayOutputStream()
    if (ctx.getMode() == SM4_ENCRYPT) {
      while (length > 0) {
        val `in` = ByteArray(16)
        val out = ByteArray(16)
        val out1 = ByteArray(16)
        bins.read(`in`)
        i = 0
        while (i < 16) {
          out[i] = (`in`[i].toInt() xor iv[i].toInt()).toByte()
          i++
        }
        sm4_one_round(ctx.getSk(), out, out1)
        System.arraycopy(out1, 0, iv, 0, 16)
        bous.write(out1)
        length -= 16
      }
    } else {
      val temp = ByteArray(16)
      while (length > 0) {
        val `in` = ByteArray(16)
        val out = ByteArray(16)
        val out1 = ByteArray(16)
        bins.read(`in`)
        System.arraycopy(`in`, 0, temp, 0, 16)
        sm4_one_round(ctx.getSk(), `in`, out)
        i = 0
        while (i < 16) {
          out1[i] = (out[i].toInt() xor iv[i].toInt()).toByte()
          i++
        }
        System.arraycopy(temp, 0, iv, 0, 16)
        bous.write(out1)
        length -= 16
      }
    }
    var output = bous.toByteArray()
    if (ctx.isPadding() && ctx.getMode() == SM4_DECRYPT) {
      output = padding(output, SM4_DECRYPT)
    }
    bins.close()
    bous.close()
    return output
  }

  companion object {
    /**
     * 加密
     */
    const val SM4_ENCRYPT = 1

    /**
     * 解密
     */
    const val SM4_DECRYPT = 0

    /**
     * AES 加密中的 S box
     */
    val SboxTable = byteArrayOf(
      0xd6.toByte(), 0x90.toByte(), 0xe9.toByte(), 0xfe.toByte(),
      0xcc.toByte(), 0xe1.toByte(), 0x3d, 0xb7.toByte(), 0x16, 0xb6.toByte(),
      0x14, 0xc2.toByte(), 0x28, 0xfb.toByte(), 0x2c, 0x05, 0x2b, 0x67,
      0x9a.toByte(), 0x76, 0x2a, 0xbe.toByte(), 0x04, 0xc3.toByte(),
      0xaa.toByte(), 0x44, 0x13, 0x26, 0x49, 0x86.toByte(), 0x06,
      0x99.toByte(), 0x9c.toByte(), 0x42, 0x50, 0xf4.toByte(), 0x91.toByte(),
      0xef.toByte(), 0x98.toByte(), 0x7a, 0x33, 0x54, 0x0b, 0x43,
      0xed.toByte(), 0xcf.toByte(), 0xac.toByte(), 0x62, 0xe4.toByte(),
      0xb3.toByte(), 0x1c, 0xa9.toByte(), 0xc9.toByte(), 0x08, 0xe8.toByte(),
      0x95.toByte(), 0x80.toByte(), 0xdf.toByte(), 0x94.toByte(), 0xfa.toByte(),
      0x75, 0x8f.toByte(), 0x3f, 0xa6.toByte(), 0x47, 0x07, 0xa7.toByte(),
      0xfc.toByte(), 0xf3.toByte(), 0x73, 0x17, 0xba.toByte(), 0x83.toByte(),
      0x59, 0x3c, 0x19, 0xe6.toByte(), 0x85.toByte(), 0x4f, 0xa8.toByte(),
      0x68, 0x6b, 0x81.toByte(), 0xb2.toByte(), 0x71, 0x64, 0xda.toByte(),
      0x8b.toByte(), 0xf8.toByte(), 0xeb.toByte(), 0x0f, 0x4b, 0x70, 0x56,
      0x9d.toByte(), 0x35, 0x1e, 0x24, 0x0e, 0x5e, 0x63, 0x58, 0xd1.toByte(),
      0xa2.toByte(), 0x25, 0x22, 0x7c, 0x3b, 0x01, 0x21, 0x78, 0x87.toByte(),
      0xd4.toByte(), 0x00, 0x46, 0x57, 0x9f.toByte(), 0xd3.toByte(), 0x27,
      0x52, 0x4c, 0x36, 0x02, 0xe7.toByte(), 0xa0.toByte(), 0xc4.toByte(),
      0xc8.toByte(), 0x9e.toByte(), 0xea.toByte(), 0xbf.toByte(), 0x8a.toByte(),
      0xd2.toByte(), 0x40, 0xc7.toByte(), 0x38, 0xb5.toByte(), 0xa3.toByte(),
      0xf7.toByte(), 0xf2.toByte(), 0xce.toByte(), 0xf9.toByte(), 0x61, 0x15,
      0xa1.toByte(), 0xe0.toByte(), 0xae.toByte(), 0x5d, 0xa4.toByte(),
      0x9b.toByte(), 0x34, 0x1a, 0x55, 0xad.toByte(), 0x93.toByte(), 0x32,
      0x30, 0xf5.toByte(), 0x8c.toByte(), 0xb1.toByte(), 0xe3.toByte(), 0x1d,
      0xf6.toByte(), 0xe2.toByte(), 0x2e, 0x82.toByte(), 0x66, 0xca.toByte(),
      0x60, 0xc0.toByte(), 0x29, 0x23, 0xab.toByte(), 0x0d, 0x53, 0x4e, 0x6f,
      0xd5.toByte(), 0xdb.toByte(), 0x37, 0x45, 0xde.toByte(), 0xfd.toByte(),
      0x8e.toByte(), 0x2f, 0x03, 0xff.toByte(), 0x6a, 0x72, 0x6d, 0x6c, 0x5b,
      0x51, 0x8d.toByte(), 0x1b, 0xaf.toByte(), 0x92.toByte(), 0xbb.toByte(),
      0xdd.toByte(), 0xbc.toByte(), 0x7f, 0x11, 0xd9.toByte(), 0x5c, 0x41,
      0x1f, 0x10, 0x5a, 0xd8.toByte(), 0x0a, 0xc1.toByte(), 0x31,
      0x88.toByte(), 0xa5.toByte(), 0xcd.toByte(), 0x7b, 0xbd.toByte(), 0x2d,
      0x74, 0xd0.toByte(), 0x12, 0xb8.toByte(), 0xe5.toByte(), 0xb4.toByte(),
      0xb0.toByte(), 0x89.toByte(), 0x69, 0x97.toByte(), 0x4a, 0x0c,
      0x96.toByte(), 0x77, 0x7e, 0x65, 0xb9.toByte(), 0xf1.toByte(), 0x09,
      0xc5.toByte(), 0x6e, 0xc6.toByte(), 0x84.toByte(), 0x18, 0xf0.toByte(),
      0x7d, 0xec.toByte(), 0x3a, 0xdc.toByte(), 0x4d, 0x20, 0x79,
      0xee.toByte(), 0x5f, 0x3e, 0xd7.toByte(), 0xcb.toByte(), 0x39, 0x48
    )

    /**
     * 外键？
     */
    val FK = intArrayOf(
      -0x5c4e453a, 0x56aa3350, 0x677d9197, -0x4d8fdd24
    )

    /**
     * 检查约束？
     */
    val CK = intArrayOf(
      0x00070e15, 0x1c232a31, 0x383f464d, 0x545b6269,
      0x70777e85, -0x736c655f, -0x57504943, -0x3b342d27,
      -0x1f18110b, -0x3fcf5ef, 0x181f262d, 0x343b4249,
      0x50575e65, 0x6c737a81, -0x77706963, -0x5b544d47,
      -0x3f38312b, -0x231c150f, -0x700f9f3, 0x141b2229,
      0x30373e45, 0x4c535a61, 0x686f767d, -0x7b746d67,
      -0x5f58514b, -0x433c352f, -0x27201913, -0xb04fdf7,
      0x10171e25, 0x2c333a41, 0x484f565d, 0x646b7279
    )
  }
}
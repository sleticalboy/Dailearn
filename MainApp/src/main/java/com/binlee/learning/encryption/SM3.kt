package com.sleticalboy.learning.encryption

object SM3 {

  val iv = byteArrayOf(
    0x73, 0x80.toByte(), 0x16, 0x6f, 0x49,
    0x14, 0xb2.toByte(), 0xb9.toByte(), 0x17, 0x24, 0x42, 0xd7.toByte(),
    0xda.toByte(), 0x8a.toByte(), 0x06, 0x00, 0xa9.toByte(), 0x6f, 0x30,
    0xbc.toByte(), 0x16.toByte(), 0x31, 0x38, 0xaa.toByte(), 0xe3.toByte(),
    0x8d.toByte(), 0xee.toByte(), 0x4d, 0xb0.toByte(), 0xfb.toByte(), 0x0e,
    0x4e
  )
  var Tj = IntArray(64)

  fun CF(V: ByteArray, B: ByteArray): ByteArray {
    val v: IntArray
    val b: IntArray
    v = convert(V)
    b = convert(B)
    return convert(CF(v, b))
  }

  private fun convert(arr: ByteArray): IntArray {
    val out = IntArray(arr.size / 4)
    val tmp = ByteArray(4)
    var i = 0
    while (i < arr.size) {
      System.arraycopy(arr, i, tmp, 0, 4)
      out[i / 4] = bigEndianByteToInt(tmp)
      i += 4
    }
    return out
  }

  private fun convert(arr: IntArray): ByteArray {
    val out = ByteArray(arr.size * 4)
    var tmp: ByteArray? = null
    for (i in arr.indices) {
      tmp = bigEndianIntToByte(arr[i])
      System.arraycopy(tmp, 0, out, i * 4, 4)
    }
    return out
  }

  fun CF(V: IntArray, B: IntArray): IntArray {
    var a: Int
    var b: Int
    var c: Int
    var d: Int
    var e: Int
    var f: Int
    var g: Int
    var h: Int
    var ss1: Int
    var ss2: Int
    var tt1: Int
    var tt2: Int
    a = V[0]
    b = V[1]
    c = V[2]
    d = V[3]
    e = V[4]
    f = V[5]
    g = V[6]
    h = V[7]
    val arr = expand(B)
    val w = arr[0]
    val w1 = arr[1]
    for (j in 0..63) {
      ss1 = bitCycleLeft(a, 12) + e + bitCycleLeft(Tj[j], j)
      ss1 = bitCycleLeft(ss1, 7)
      ss2 = ss1 xor bitCycleLeft(a, 12)
      tt1 = FFj(a, b, c, j) + d + ss2 + w1[j]
      tt2 = GGj(e, f, g, j) + h + ss1 + w[j]
      d = c
      c = bitCycleLeft(b, 9)
      b = a
      a = tt1
      h = g
      g = bitCycleLeft(f, 19)
      f = e
      e = P0(tt2)

      /*System.out.print(j+" ");
      System.out.print(Integer.toHexString(a)+" ");
System.out.print(Integer.toHexString(b)+" ");
System.out.print(Integer.toHexString(c)+" ");
System.out.print(Integer.toHexString(d)+" ");
System.out.print(Integer.toHexString(e)+" ");
System.out.print(Integer.toHexString(f)+" ");
System.out.print(Integer.toHexString(g)+" ");
System.out.print(Integer.toHexString(h)+" ");
System.out.println("");*/
    }
    //		System.out.println("");
    val out = IntArray(8)
    out[0] = a xor V[0]
    out[1] = b xor V[1]
    out[2] = c xor V[2]
    out[3] = d xor V[3]
    out[4] = e xor V[4]
    out[5] = f xor V[5]
    out[6] = g xor V[6]
    out[7] = h xor V[7]
    return out
  }

  private fun expand(B: IntArray): Array<IntArray> {
    val W = IntArray(68)
    val W1 = IntArray(64)
    for (i in B.indices) {
      W[i] = B[i]
    }
    for (i in 16..67) {
      W[i] = (P1(W[i - 16] xor W[i - 9] xor bitCycleLeft(W[i - 3], 15))
          xor bitCycleLeft(W[i - 13], 7) xor W[i - 6])
    }
    for (i in 0..63) {
      W1[i] = W[i] xor W[i + 4]
    }
    return arrayOf(W, W1)
  }

  private fun bigEndianIntToByte(num: Int): ByteArray {
    return back(Util.intToBytes(num))
  }

  private fun bigEndianByteToInt(bytes: ByteArray): Int {
    return Util.byteToInt(back(bytes))
  }

  private fun FFj(X: Int, Y: Int, Z: Int, j: Int): Int {
    return if (j in 0..15) {
      FF1j(X, Y, Z)
    } else {
      FF2j(X, Y, Z)
    }
  }

  private fun GGj(X: Int, Y: Int, Z: Int, j: Int): Int {
    return if (j in 0..15) {
      GG1j(X, Y, Z)
    } else {
      GG2j(X, Y, Z)
    }
  }

  // 逻辑位运算函数
  private fun FF1j(X: Int, Y: Int, Z: Int): Int {
    return X xor Y xor Z
  }

  private fun FF2j(X: Int, Y: Int, Z: Int): Int {
    return X and Y or (X and Z) or (Y and Z)
  }

  private fun GG1j(X: Int, Y: Int, Z: Int): Int {
    return X xor Y xor Z
  }

  private fun GG2j(X: Int, Y: Int, Z: Int): Int {
    return X and Y or (X.inv() and Z)
  }

  private fun P0(X: Int): Int {
    var y = rotateLeft(X, 9)
    y = bitCycleLeft(X, 9)
    var z = rotateLeft(X, 17)
    z = bitCycleLeft(X, 17)
    return X xor y xor z
  }

  private fun P1(X: Int): Int {
    return X xor bitCycleLeft(X, 15) xor bitCycleLeft(X, 23)
  }

  /**
   * 对最后一个分组字节数据padding
   *
   * @param in
   * @param bLen 分组个数
   * @return
   */
  fun padding(`in`: ByteArray, bLen: Int): ByteArray {
    var k = 448 - (8 * `in`.size + 1) % 512
    if (k < 0) {
      k = 960 - (8 * `in`.size + 1) % 512
    }
    k += 1
    val padd = ByteArray(k / 8)
    padd[0] = 0x80.toByte()
    val n = `in`.size * 8 + bLen * 512.toLong()
    val out = ByteArray(`in`.size + k / 8 + 64 / 8)
    var pos = 0
    System.arraycopy(`in`, 0, out, 0, `in`.size)
    pos += `in`.size
    System.arraycopy(padd, 0, out, pos, padd.size)
    pos += padd.size
    val tmp = back(Util.longToBytes(n))
    System.arraycopy(tmp, 0, out, pos, tmp.size)
    return out
  }

  /**
   * 字节数组逆序
   *
   * @param in
   * @return
   */
  private fun back(`in`: ByteArray?): ByteArray {
    val out = ByteArray(`in`!!.size)
    for (i in out.indices) {
      out[i] = `in`[out.size - i - 1]
    }
    return out
  }

  fun rotateLeft(x: Int, n: Int): Int {
    return x shl n or (x shr 32 - n)
  }

  private fun bitCycleLeft(n: Int, bitLen: Int): Int {
    var bitLen = bitLen
    bitLen %= 32
    var tmp = bigEndianIntToByte(n)
    val byteLen = bitLen / 8
    val len = bitLen % 8
    if (byteLen > 0) {
      tmp = byteCycleLeft(tmp, byteLen)
    }
    if (len > 0) {
      tmp = bitSmall8CycleLeft(tmp, len)
    }
    return bigEndianByteToInt(tmp)
  }

  private fun bitSmall8CycleLeft(`in`: ByteArray, len: Int): ByteArray {
    val tmp = ByteArray(`in`.size)
    var t1: Int
    var t2: Int
    var t3: Int
    for (i in tmp.indices) {
      t1 = (`in`[i].toInt() and 0x000000ff shl len)
      t2 = (`in`[(i + 1) % tmp.size].toInt() and 0x000000ff shr 8 - len)
      t3 = (t1 or t2)
      tmp[i] = t3.toByte()
    }
    return tmp
  }

  private fun byteCycleLeft(`in`: ByteArray, byteLen: Int): ByteArray {
    val tmp = ByteArray(`in`.size)
    System.arraycopy(`in`, byteLen, tmp, 0, `in`.size - byteLen)
    System.arraycopy(`in`, 0, tmp, `in`.size - byteLen, byteLen)
    return tmp
  }

  init {
    for (i in 0..15) {
      Tj[i] = 0x79cc4519
    }
    for (i in 16..63) {
      Tj[i] = 0x7a879d8a
    }
  }
}
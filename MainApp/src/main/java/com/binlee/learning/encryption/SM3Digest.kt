package com.binlee.learning.encryption

import com.binlee.learning.util.OperationUtils
import com.binlee.learning.util.UnicodeUtils.string2char
import org.bouncycastle.util.encoders.Hex

class SM3Digest {

  /**
   * 缓冲区
   */
  private val xBuf = ByteArray(BUFFER_LENGTH)

  /**
   * 缓冲区偏移量
   */
  private var xBufOff = 0

  /**
   * 初始向量
   */
  private var V = SM3.iv.clone()
  private var cntBlock = 0

  constructor()
  constructor(t: SM3Digest?) {
    System.arraycopy(t!!.xBuf, 0, xBuf, 0, t.xBuf.size)
    xBufOff = t.xBufOff
    System.arraycopy(t.V, 0, V, 0, t.V.size)
  }

  /**
   * SM3结果输出
   *
   * @param out    保存SM3结构的缓冲区
   * @param outOff 缓冲区偏移量
   * @return
   */
  fun doFinal(out: ByteArray?, outOff: Int): Int {
    val tmp = doFinal()
    System.arraycopy(tmp, 0, out, 0, tmp.size)
    return BYTE_LENGTH
  }

  fun reset() {
    xBufOff = 0
    cntBlock = 0
    V = SM3.iv.clone()
  }

  /**
   * 明文输入
   *
   * @param in    明文输入缓冲区
   * @param inOff 缓冲区偏移量
   * @param len   明文长度
   */
  fun update(`in`: ByteArray?, inOff: Int, len: Int) {
    val partLen = BUFFER_LENGTH - xBufOff
    var inputLen = len
    var dPos = inOff
    if (partLen < inputLen) {
      System.arraycopy(`in`!!, dPos, xBuf, xBufOff, partLen)
      inputLen -= partLen
      dPos += partLen
      doUpdate()
      while (inputLen > BUFFER_LENGTH) {
        System.arraycopy(`in`, dPos, xBuf, 0, BUFFER_LENGTH)
        inputLen -= BUFFER_LENGTH
        dPos += BUFFER_LENGTH
        doUpdate()
      }
    }
    System.arraycopy(`in`!!, dPos, xBuf, xBufOff, inputLen)
    xBufOff += inputLen
  }

  private fun doUpdate() {
    val B = ByteArray(BLOCK_LENGTH)
    var i = 0
    while (i < BUFFER_LENGTH) {
      System.arraycopy(xBuf, i, B, 0, B.size)
      doHash(B)
      i += BLOCK_LENGTH
    }
    xBufOff = 0
  }

  private fun doHash(B: ByteArray) {
    val tmp = SM3.CF(V, B)
    System.arraycopy(tmp, 0, V, 0, V.size)
    cntBlock++
  }

  private fun doFinal(): ByteArray {
    val B = ByteArray(BLOCK_LENGTH)
    val buffer = ByteArray(xBufOff)
    System.arraycopy(xBuf, 0, buffer, 0, buffer.size)
    val tmp = SM3.padding(buffer, cntBlock)
    var i = 0
    while (i < tmp.size) {
      System.arraycopy(tmp, i, B, 0, B.size)
      doHash(B)
      i += BLOCK_LENGTH
    }
    return V
  }

  fun update(`in`: Byte) {
    val buffer = byteArrayOf(`in`)
    update(buffer, 0, 1)
  }

  fun getDigestSize(): Int {
    return BYTE_LENGTH
  }

  //	public static void main(String[] args)
  //	{
  //		byte[] md = new byte[32];
  //		byte[] msg1 = "ererfeiisgod".getBytes();
  //		SM3Digest sm3 = new SM3Digest();
  //		sm3.update(msg1, 0, msg1.length);
  //		sm3.doFinal(md, 0);
  //		String s = new String(Hex.encode(md));
  //		System.out.println(s.toUpperCase());
  //	}
  fun getKey(inputStr: String): String {
    val md = ByteArray(32)
    val msg1 = inputStr.toByteArray()
    val sm3 = SM3Digest()
    sm3.update(msg1, 0, msg1.size)
    sm3.doFinal(md, 0)
    val s = String(Hex.encode(md))
    val c = string2char(inputStr)
    val finalStr = OperationUtils.convertKey(s, c)
    var hahaStr = ""
    val arr = finalStr.toCharArray()
    for (i in 0 until arr.size / 2) {
      val integer1 = arr[i].code
      val integer2 = arr[arr.size - 1 - i].code
      val integer = (integer1 + integer2) % 95 + 32
      val ch = integer.toChar()
      hahaStr += ch
      println("integer1:$integer1 integer2:$integer2 integer:$integer")
    }
    println("s:" + s + "  s.length:" + s.length)
    println("hahaStr:" + hahaStr + "  hahaStr.length:" + hahaStr.length)
    return hahaStr.substring(4, 20)
  }

  companion object {
    /**
     * SM3值的长度
     */
    private const val BYTE_LENGTH = 32

    /**
     * SM3分组长度
     */
    private const val BLOCK_LENGTH = 64

    /**
     * 缓冲区长度
     */
    private const val BUFFER_LENGTH = BLOCK_LENGTH * 1
  }
}
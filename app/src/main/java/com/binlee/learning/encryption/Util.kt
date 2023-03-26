package com.binlee.learning.encryption

import java.math.BigInteger
import java.util.*
import kotlin.math.pow

object Util {
  /**
   * 整形转换成网络传输的字节流（字节数组）型数据
   *
   * @param num 一个整型数据
   * @return 4个字节的自己数组
   */
  fun intToBytes(num: Int): ByteArray {
    val bytes = ByteArray(4)
    bytes[0] = (0xff and (num shr 0)).toByte()
    bytes[1] = (0xff and (num shr 8)).toByte()
    bytes[2] = (0xff and (num shr 16)).toByte()
    bytes[3] = (0xff and (num shr 24)).toByte()
    return bytes
  }

  /**
   * 四个字节的字节数据转换成一个整形数据
   *
   * @param bytes 4个字节的字节数组
   * @return 一个整型数据
   */
  fun byteToInt(bytes: ByteArray): Int {
    var num = 0
    var temp = 0x000000ff and bytes[0].toInt() shl 0
    num = num or temp
    temp = 0x000000ff and bytes[1].toInt() shl 8
    num = num or temp
    temp = 0x000000ff and bytes[2].toInt() shl 16
    num = num or temp
    temp = 0x000000ff and bytes[3].toInt() shl 24
    num = num or temp
    return num
  }

  /**
   * 长整形转换成网络传输的字节流（字节数组）型数据
   *
   * @param num 一个长整型数据
   * @return 4个字节的自己数组
   */
  fun longToBytes(num: Long): ByteArray {
    val bytes = ByteArray(8)
    for (i in 0..7) {
      bytes[i] = (0xff and (num shr i * 8).toInt()).toByte()
    }
    return bytes
  }

  /**
   * 大数字转换字节流（字节数组）型数据
   *
   * @param n
   * @return
   */
  fun byteConvert32Bytes(n: BigInteger?): ByteArray? {
    var tmpd = null as ByteArray?
    if (n == null) {
      return null
    }
    if (n.toByteArray().size == 33) {
      tmpd = ByteArray(32)
      System.arraycopy(n.toByteArray(), 1, tmpd, 0, 32)
    } else if (n.toByteArray().size == 32) {
      tmpd = n.toByteArray()
    } else {
      tmpd = ByteArray(32)
      for (i in 0 until 32 - n.toByteArray().size) {
        tmpd[i] = 0
      }
      System.arraycopy(n.toByteArray(), 0, tmpd, 32 - n.toByteArray().size, n.toByteArray().size)
    }
    return tmpd
  }

  /**
   * 换字节流（字节数组）型数据转大数字
   *
   * @param b
   * @return
   */
  fun byteConvertInteger(b: ByteArray): BigInteger {
    if (b[0] < 0) {
      val temp = ByteArray(b.size + 1)
      temp[0] = 0
      System.arraycopy(b, 0, temp, 1, b.size)
      return BigInteger(temp)
    }
    return BigInteger(b)
  }

  /**
   * 根据字节数组获得值(十六进制数字)
   *
   * @param bytes
   * @return
   */
  fun getHexString(bytes: ByteArray): String {
    return getHexString(bytes, true)
  }

  /**
   * 根据字节数组获得值(十六进制数字)
   *
   * @param bytes
   * @param upperCase
   * @return
   */
  fun getHexString(bytes: ByteArray, upperCase: Boolean): String {
    var ret = ""
    for (i in bytes.indices) {
      ret += ((bytes[i].toInt() and 0xff) + 0x100).toString(16).substring(1)
    }
    return if (upperCase) ret.uppercase(Locale.getDefault()) else ret
  }

  /**
   * 打印十六进制字符串
   *
   * @param bytes
   */
  fun printHexString(bytes: ByteArray) {
    for (i in bytes.indices) {
      var hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
      if (hex.length == 1) {
        hex = "0$hex"
      }
      print("0x" + hex.uppercase(Locale.getDefault()) + ",")
    }
    println("")
  }

  /**
   * Convert hex string to byte[]
   *
   * @param hexString the hex string
   * @return byte[]
   */
  fun hexStringToBytes(hexString: String?): ByteArray? {
    var input = hexString
    if (input == null || input == "") {
      return null
    }
    input = input.uppercase(Locale.getDefault())
    val length = input.length / 2
    val hexChars = input.toCharArray()
    val d = ByteArray(length)
    for (i in 0 until length) {
      val pos = i * 2
      d[i] =
        (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
    }
    return d
  }

  /**
   * Convert char to byte
   *
   * @param c char
   * @return byte
   */
  fun charToByte(c: Char): Byte {
    return "0123456789ABCDEF".indexOf(c).toByte()
  }

  /**
   * 用于建立十六进制字符的输出的小写字符数组
   */
  private val DIGITS_LOWER = charArrayOf(
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  )

  /**
   * 用于建立十六进制字符的输出的大写字符数组
   */
  private val DIGITS_UPPER = charArrayOf(
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  )
  /**
   * 将字节数组转换为十六进制字符数组
   *
   * @param data        byte[]
   * @param toLowerCase `true` 传换成小写格式 ， `false` 传换成大写格式
   * @return 十六进制char[]
   */
  /**
   * 将字节数组转换为十六进制字符数组
   *
   * @param data byte[]
   * @return 十六进制char[]
   */
  @JvmOverloads
  fun encodeHex(data: ByteArray, toLowerCase: Boolean = true): CharArray {
    return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
  }

  /**
   * 将字节数组转换为十六进制字符数组
   *
   * @param data     byte[]
   * @param toDigits 用于控制输出的char[]
   * @return 十六进制char[]
   */
  internal fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
    val l = data.size
    val out = CharArray(l shl 1)
    // two characters form the hex value.
    var i = 0
    var j = 0
    while (i < l) {
      out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
      out[j++] = toDigits[0x0F and data[i].toInt()]
      i++
    }
    return out
  }
  /**
   * 将字节数组转换为十六进制字符串
   *
   * @param data        byte[]
   * @param toLowerCase `true` 传换成小写格式 ， `false` 传换成大写格式
   * @return 十六进制String
   */
  /**
   * 将字节数组转换为十六进制字符串
   *
   * @param data byte[]
   * @return 十六进制String
   */
  @JvmOverloads
  fun encodeHexString(data: ByteArray, toLowerCase: Boolean = true): String {
    return encodeHexString(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
  }

  /**
   * 将字节数组转换为十六进制字符串
   *
   * @param data     byte[]
   * @param toDigits 用于控制输出的char[]
   * @return 十六进制String
   */
  internal fun encodeHexString(data: ByteArray, toDigits: CharArray): String {
    return String(encodeHex(data, toDigits))
  }

  /**
   * 将十六进制字符数组转换为字节数组
   *
   * @param data 十六进制char[]
   * @return byte[]
   * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
   */
  fun decodeHex(data: CharArray): ByteArray {
    val len = data.size
    if (len and 0x01 != 0) {
      throw RuntimeException("Odd number of characters.")
    }
    val out = ByteArray(len shr 1)

    // two characters form the hex value.
    var i = 0
    var j = 0
    while (j < len) {
      var f = toDigit(data[j], j) shl 4
      j++
      f = f or toDigit(data[j], j)
      j++
      out[i] = (f and 0xFF).toByte()
      i++
    }
    return out
  }

  /**
   * 将十六进制字符转换成一个整数
   *
   * @param ch    十六进制char
   * @param index 十六进制字符在字符数组中的位置
   * @return 一个整数
   * @throws RuntimeException 当ch不是一个合法的十六进制字符时，抛出运行时异常
   */
  internal fun toDigit(ch: Char, index: Int): Int {
    val digit = Character.digit(ch, 16)
    if (digit == -1) {
      throw RuntimeException(
        "Illegal hexadecimal character " + ch
            + " at index " + index
      )
    }
    return digit
  }

  /**
   * 数字字符串转ASCII码字符串
   *
   * @param content 字符串
   * @return ASCII字符串
   */
  fun StringToAsciiString(content: String): String {
    var result = ""
    val max = content.length
    for (i in 0 until max) {
      val c = content[i]
      val b = Integer.toHexString(c.code)
      result += b
    }
    return result
  }

  /**
   * 十六进制转字符串
   *
   * @param hexString  十六进制字符串
   * @param encodeType 编码类型4：Unicode，2：普通编码
   * @return 字符串
   */
  fun hexStringToString(hexString: String, encodeType: Int): String {
    var result = ""
    val max = hexString.length / encodeType
    for (i in 0 until max) {
      val c = hexStringToAlgorism(
        hexString
          .substring(i * encodeType, (i + 1) * encodeType)
      ).toChar()
      result += c
    }
    return result
  }

  /**
   * 十六进制字符串装十进制
   *
   * @param hex 十六进制字符串
   * @return 十进制数值
   */
  fun hexStringToAlgorism(hex: String): Int {
    var input = hex
    input = input.uppercase(Locale.getDefault())
    val max = input.length
    var result = 0
    for (i in max downTo 1) {
      val c = input[i - 1]
      val algorism = if (c in '0'..'9') {
        c - '0'
      } else {
        c.code - 55
      }
      result += (16.0.pow(max - i.toDouble()) * algorism).toInt()
    }
    return result
  }

  /**
   * 十六转二进制
   *
   * @param hex 十六进制字符串
   * @return 二进制字符串
   */
  fun hexStringToBinary(hex: String): String {
    val input = hex.uppercase(Locale.getDefault())
    var result = ""
    val max = input.length
    for (i in 0 until max) {
      when (input[i]) {
        '0' -> result += "0000"
        '1' -> result += "0001"
        '2' -> result += "0010"
        '3' -> result += "0011"
        '4' -> result += "0100"
        '5' -> result += "0101"
        '6' -> result += "0110"
        '7' -> result += "0111"
        '8' -> result += "1000"
        '9' -> result += "1001"
        'A' -> result += "1010"
        'B' -> result += "1011"
        'C' -> result += "1100"
        'D' -> result += "1101"
        'E' -> result += "1110"
        'F' -> result += "1111"
      }
    }
    return result
  }

  /**
   * ASCII码字符串转数字字符串
   *
   * @param content ASCII字符串
   * @return 字符串
   */
  fun AsciiStringToString(content: String): String {
    var result = ""
    val length = content.length / 2
    for (i in 0 until length) {
      val c = content.substring(i * 2, i * 2 + 2)
      val a = hexStringToAlgorism(c)
      val b = a.toChar()
      val d = b.toString()
      result += d
    }
    return result
  }

  /**
   * 将十进制转换为指定长度的十六进制字符串
   *
   * @param algorism  int 十进制数字
   * @param maxLength int 转换后的十六进制字符串长度
   * @return String 转换后的十六进制字符串
   */
  fun algorismToHexString(algorism: Int, maxLength: Int): String {
    var result = ""
    result = Integer.toHexString(algorism)
    if (result.length % 2 == 1) {
      result = "0$result"
    }
    return patchHexString(result.uppercase(Locale.getDefault()), maxLength)
  }

  /**
   * 字节数组转为普通字符串（ASCII对应的字符）
   *
   * @param bytearray byte[]
   * @return String
   */
  fun byteToString(bytearray: ByteArray): String {
    var result = ""
    var temp: Char
    val length = bytearray.size
    for (i in 0 until length) {
      temp = bytearray[i].toInt().toChar()
      result += temp
    }
    return result
  }

  /**
   * 二进制字符串转十进制
   *
   * @param binary 二进制字符串
   * @return 十进制数值
   */
  fun binaryToAlgorism(binary: String): Int {
    val max = binary.length
    var result = 0
    for (i in max downTo 1) {
      val c = binary[i - 1]
      val algorism = c - '0'
      result += (2.0.pow(max - i.toDouble()) * algorism).toInt()
    }
    return result
  }

  /**
   * 十进制转换为十六进制字符串
   *
   * @param algorism int 十进制的数字
   * @return String 对应的十六进制字符串
   */
  fun algorismToHEXString(algorism: Int): String {
    var result = ""
    result = Integer.toHexString(algorism)
    if (result.length % 2 == 1) {
      result = "0$result"
    }
    result = result.uppercase(Locale.getDefault())
    return result
  }

  /**
   * HEX字符串前补0，主要用于长度位数不足。
   *
   * @param str       String 需要补充长度的十六进制字符串
   * @param maxLength int 补充后十六进制字符串的长度
   * @return 补充结果
   */
  fun patchHexString(str: String, maxLength: Int): String {
    var result = str
    var temp = ""
    for (i in 0 until maxLength - result.length) {
      temp = "0$temp"
    }
    result = (temp + result).substring(0, maxLength)
    return result
  }

  /**
   * 将一个字符串转换为int
   *
   * @param s          String 要转换的字符串
   * @param defaultInt int 如果出现异常,默认返回的数字
   * @param radix      int 要转换的字符串是什么进制的,如16 8 10.
   * @return int 转换后的数字
   */
  fun parseToInt(s: String, defaultInt: Int, radix: Int): Int {
    var i = 0
    i = try {
      s.toInt(radix)
    } catch (ex: NumberFormatException) {
      defaultInt
    }
    return i
  }

  /**
   * 将一个十进制形式的数字字符串转换为int
   *
   * @param s          String 要转换的字符串
   * @param defaultInt int 如果出现异常,默认返回的数字
   * @return int 转换后的数字
   */
  fun parseToInt(s: String, defaultInt: Int): Int {
    var i = 0
    i = try {
      s.toInt()
    } catch (ex: NumberFormatException) {
      defaultInt
    }
    return i
  }

  /**
   * 十六进制串转化为byte数组
   *
   * @return the array of byte
   */
  @Throws(IllegalArgumentException::class)
  fun hexToByte(hex: String): ByteArray {
    require(hex.length % 2 == 0)
    val arr = hex.toCharArray()
    val b = ByteArray(hex.length / 2)
    var i = 0
    var j = 0
    val l = hex.length
    while (i < l) {
      val swap = "" + arr[i++] + arr[i]
      b[j] = (swap.toInt(16) and 0xFF).toByte()
      i++
      j++
    }
    return b
  }

  /**
   * 字节数组转换为十六进制字符串
   *
   * @param b byte[] 需要转换的字节数组
   * @return String 十六进制字符串
   */
  fun byteToHex(b: ByteArray?): String {
    requireNotNull(b) { "Argument b ( byte array ) is null! " }
    var hs = ""
    var stmp = ""
    for (n in b.indices) {
      stmp = Integer.toHexString(b[n].toInt() and 0xff)
      hs = if (stmp.length == 1) {
        hs + "0" + stmp
      } else {
        hs + stmp
      }
    }
    return hs.uppercase(Locale.getDefault())
  }

  fun subByte(input: ByteArray, startIndex: Int, length: Int): ByteArray {
    val bt = ByteArray(length)
    for (i in 0 until length) {
      bt[i] = input[i + startIndex]
    }
    return bt
  }
}
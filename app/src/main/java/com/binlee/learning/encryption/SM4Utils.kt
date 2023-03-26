package com.binlee.learning.encryption

import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder
import java.util.regex.Pattern

class SM4Utils {

  // 秘钥
  private var secretKey = ""

  // 向量
  private val iv = ""

  // 16 进制字符串
  private var hexString = false

  /**
   * 加密（电子密码本模式）
   *
   * @param plainText 明文
   * @return 密文
   */
  fun encryptECB(plainText: String): String? {
    return try {
      val ctx = SM4Context()
      ctx.setPadding(true)
      ctx.setMode(SM4.SM4_ENCRYPT)
      val keyBytes = if (hexString) {
        Util.hexStringToBytes(secretKey)
      } else {
        secretKey.toByteArray()
      }
      val sm4 = SM4()
      sm4.setEncryptKey(ctx, keyBytes)
      val input = plainText.toByteArray(charset("GBK"))
      val encrypted = sm4.cryptECB(ctx, input)
      var cipherText = BASE64Encoder().encode(encrypted)
      if (cipherText != null && cipherText.trim { it <= ' ' }.isNotEmpty()) {
        val p = Pattern.compile("\\s*|\t|\r|\n")
        val m = p.matcher(cipherText)
        cipherText = m.replaceAll("")
      }
      cipherText
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * 解密（电子密码本模式）
   *
   * @param cipherText 密文
   * @return 明文
   */
  fun decryptECB(cipherText: String?): String? {
    return try {
      val ctx = SM4Context()
      ctx.setPadding(true)
      ctx.setMode(SM4.SM4_DECRYPT)
      val keyBytes = if (hexString) {
        Util.hexStringToBytes(secretKey)
      } else {
        secretKey.toByteArray()
      }
      val sm4 = SM4()
      sm4.setDecryptKey(ctx, keyBytes)
      val decrypted = sm4.cryptECB(ctx, BASE64Decoder().decodeBuffer(cipherText))
      String(decrypted!!, charset("GBK"))
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * (加密块链模式)
   *
   * @param plainText
   * @return
   */
  fun encryptCBC(plainText: String): String? {
    return try {
      val ctx = SM4Context()
      ctx.setPadding(true)
      ctx.setMode(SM4.SM4_ENCRYPT)
      val keyBytes: ByteArray?
      val ivBytes: ByteArray?
      if (hexString) {
        keyBytes = Util.hexStringToBytes(secretKey)
        ivBytes = Util.hexStringToBytes(iv)
      } else {
        keyBytes = secretKey.toByteArray()
        ivBytes = iv.toByteArray()
      }
      val sm4 = SM4()
      sm4.setEncryptKey(ctx, keyBytes)
      var cipherText = BASE64Encoder().encode(
        sm4.cryptCBC(
          ctx, ivBytes,
          plainText.toByteArray(charset("GBK"))
        )
      )
      if (cipherText != null && cipherText.trim { it <= ' ' }.isNotEmpty()) {
        val p = Pattern.compile("\\s*|\t|\r|\n")
        val m = p.matcher(cipherText)
        cipherText = m.replaceAll("")
      }
      cipherText
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * (加密块链模式)
   *
   * @param cipherText
   * @return
   */
  fun decryptCBC(cipherText: String?): String? {
    return try {
      val ctx = SM4Context()
      ctx.setPadding(true)
      ctx.setMode(SM4.SM4_DECRYPT)
      val keyBytes: ByteArray?
      val ivBytes: ByteArray?
      if (hexString) {
        keyBytes = Util.hexStringToBytes(secretKey)
        ivBytes = Util.hexStringToBytes(iv)
      } else {
        keyBytes = secretKey.toByteArray()
        ivBytes = iv.toByteArray()
      }
      val sm4 = SM4()
      sm4.setDecryptKey(ctx, keyBytes)
      val decrypted = sm4.cryptCBC(ctx, ivBytes, BASE64Decoder().decodeBuffer(cipherText))
      String(decrypted!!, charset("GBK"))
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * 加密
   */
  fun getEncryptStr(inputStr: String, secretKey: String): String? {
    this.secretKey = secretKey //meF8U9wHFOMfs2Y9
    hexString = false
    println("ECB模式")
    val cipherText = encryptECB(inputStr)
    println("ECB模式")
    return cipherText
  }

  /**
   * 解密
   */
  fun getDecryptStr(inputStr: String?, secretKey: String): String? {
    this.secretKey = secretKey // meF8U9wHFOMfs2Y9
    hexString = false
    return decryptECB(inputStr)
  }

  companion object {
    private val instance = SM4Utils()
    fun getInstance(): SM4Utils {
      return instance
    }
  }
}
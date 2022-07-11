package com.sleticalboy.learning.encryption

import com.sleticalboy.util.OperationUtils
import com.sleticalboy.util.UnicodeUtils.string2char
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import java.io.IOException
import java.math.BigInteger

class SM2Utils {

  //	public static void main(String[] args) throws Exception
  //	{
  //		//生成密钥对
  //		generateKeyPair();
  //
  //		String plainText = "ererfeiisgod";
  //		byte[] sourceData = plainText.getBytes();
  //
  //		//下面的秘钥可以使用generateKeyPair()生成的秘钥内容
  //		// 国密规范正式私钥
  //		String prik = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
  //		// 国密规范正式公钥
  //		String pubk = "04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A";
  //
  //		System.out.println("加密: ");
  //		String cipherText = SM2Utils.encrypt(Util.hexToByte(pubk), sourceData);
  //		System.out.println(cipherText);
  //		System.out.println("解密: ");
  //		plainText = new String(SM2Utils.decrypt(Util.hexToByte(prik), Util.hexToByte(cipherText)));
  //		System.out.println(plainText);
  //
  //	}
  fun getEncStr(plainText: String): String {
    generateKeyPair()
    //国密规范正式私钥
    //String prik = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
    //国密规范正式公钥
    val pubk =
      "04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A"
    val sourceData = plainText.toByteArray()
    val c = string2char(plainText)
    try {
      //加密后的String！
      val cipherText = encrypt(Util.hexToByte(pubk), sourceData)
      //与't'异或
      val finalStr = OperationUtils.convertKey(cipherText, c)

      //			Log.e("plainText:", plainText);
      //			Log.e("c:",""+c);
      //			Log.e("cipherText:", cipherText);
      //			Log.e("finalStr~16:", finalStr.substring(0 ,16));
      return finalStr.substring(0, 16)
    } catch (e: IllegalArgumentException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return ""
  }

  fun getDecStr(cipherText: String): String {
    //国密规范正式私钥
    val prik = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94"
    println("解密: ")
    try {
      return String(decrypt(Util.hexToByte(prik), Util.hexToByte(cipherText))!!)
    } catch (e: IllegalArgumentException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return ""
  }

  companion object {
    //生成随机秘钥对
    fun generateKeyPair() {
      val sm2: SM2 = SM2.Companion.Instance()
      val key = sm2.ecc_key_pair_generator.generateKeyPair()
      val ecpriv = key.private as ECPrivateKeyParameters
      val ecpub = key.public as ECPublicKeyParameters
      val privateKey = ecpriv.d
      val publicKey = ecpub.q
      println("公钥: " + Util.byteToHex(publicKey.encoded))
      println("私钥: " + Util.byteToHex(privateKey.toByteArray()))
    }

    //数据加密
    @Throws(IOException::class)
    fun encrypt(publicKey: ByteArray?, data: ByteArray?): String? {
      if (publicKey == null || publicKey.size == 0) {
        return null
      }
      if (data == null || data.size == 0) {
        return null
      }
      val source = ByteArray(data.size)
      System.arraycopy(data, 0, source, 0, data.size)
      val cipher = Cipher()
      val sm2: SM2 = SM2.Companion.Instance()
      val userKey = sm2.ecc_curve.decodePoint(publicKey)
      val c1 = cipher.Init_enc(sm2, userKey)
      cipher.Encrypt(source)
      val c3 = ByteArray(32)
      cipher.Dofinal(c3)

      //		System.out.println("C1 " + Util.byteToHex(c1.getEncoded()));
      //		System.out.println("C2 " + Util.byteToHex(source));
      //		System.out.println("C3 " + Util.byteToHex(c3));
      //C1 C2 C3拼装成加密字串
      return Util.byteToHex(c1.encoded) + Util.byteToHex(source) + Util.byteToHex(c3)
    }

    //数据解密
    @Throws(IOException::class)
    fun decrypt(privateKey: ByteArray?, encryptedData: ByteArray?): ByteArray? {
      if (privateKey == null || privateKey.size == 0) {
        return null
      }
      if (encryptedData == null || encryptedData.size == 0) {
        return null
      }
      //加密字节数组转换为十六进制的字符串 长度变为encryptedData.length * 2
      val data = Util.byteToHex(encryptedData)

      /***分解加密字串
       * （C1 = C1标志位2位 + C1实体部分128位 = 130）
       * （C3 = C3实体部分64位  = 64）
       * （C2 = encryptedData.length * 2 - C1长度  - C2长度）
       */
      val c1Bytes = Util.hexToByte(data.substring(0, 130))
      val c2Len = encryptedData.size - 97
      val c2 = Util.hexToByte(data.substring(130, 130 + 2 * c2Len))
      val c3 = Util.hexToByte(data.substring(130 + 2 * c2Len, 194 + 2 * c2Len))
      val sm2: SM2 = SM2.Companion.Instance()
      val userD = BigInteger(1, privateKey)

      //通过C1实体字节来生成ECPoint
      val c1 = sm2.ecc_curve.decodePoint(c1Bytes)
      val cipher = Cipher()
      cipher.Init_dec(userD, c1)
      cipher.Decrypt(c2)
      cipher.Dofinal(c3)

      //返回解密结果
      return c2
    }
  }
}
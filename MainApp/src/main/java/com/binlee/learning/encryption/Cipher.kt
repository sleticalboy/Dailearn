package com.sleticalboy.learning.encryption

import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

class Cipher {

  private var ct = 1
  private var p2: ECPoint? = null
  private var sm3keybase: SM3Digest? = null
  private var sm3c3: SM3Digest? = null
  private val key: ByteArray = ByteArray(32)
  private var keyOff: Byte = 0

  private fun Reset() {
    sm3keybase = SM3Digest()
    sm3c3 = SM3Digest()
    var p = Util.byteConvert32Bytes(p2!!.x.toBigInteger())
    sm3keybase!!.update(p, 0, p!!.size)
    sm3c3!!.update(p, 0, p.size)
    p = Util.byteConvert32Bytes(p2!!.y.toBigInteger())
    sm3keybase!!.update(p, 0, p!!.size)
    ct = 1
    NextKey()
  }

  private fun NextKey() {
    val sm3keycur = SM3Digest(sm3keybase)
    sm3keycur.update((ct shr 24 and 0xff).toByte())
    sm3keycur.update((ct shr 16 and 0xff).toByte())
    sm3keycur.update((ct shr 8 and 0xff).toByte())
    sm3keycur.update((ct and 0xff).toByte())
    sm3keycur.doFinal(key, 0)
    keyOff = 0
    ct++
  }

  fun Init_enc(sm2: SM2, userKey: ECPoint): ECPoint {
    val key = sm2.ecc_key_pair_generator.generateKeyPair()
    val ecpriv = key.private as ECPrivateKeyParameters
    val ecpub = key.public as ECPublicKeyParameters
    val k = ecpriv.d
    val c1 = ecpub.q
    p2 = userKey.multiply(k)
    Reset()
    return c1
  }

  fun Encrypt(data: ByteArray) {
    sm3c3!!.update(data, 0, data.size)
    for (i in data.indices) {
      if (keyOff.toInt() == key.size) {
        NextKey()
      }
      data[i] = (data[i].toInt() xor key[keyOff++.toInt()].toInt()).toByte()
    }
  }

  fun Init_dec(userD: BigInteger?, c1: ECPoint) {
    p2 = c1.multiply(userD)
    Reset()
  }

  fun Decrypt(data: ByteArray?) {
    for (i in data!!.indices) {
      if (keyOff.toInt() == key.size) {
        NextKey()
      }
      data[i] = (data[i].toInt() xor key[keyOff++.toInt()].toInt()).toByte()
    }
    sm3c3!!.update(data, 0, data.size)
  }

  fun Dofinal(c3: ByteArray?) {
    val p = Util.byteConvert32Bytes(p2!!.y.toBigInteger())
    sm3c3!!.update(p, 0, p!!.size)
    sm3c3!!.doFinal(c3, 0)
    Reset()
  }
}
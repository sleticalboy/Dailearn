package com.binlee.learning.encryption

import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECFieldElement
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.SecureRandom

class SM2 {

  val ecc_p: BigInteger
  val ecc_a: BigInteger
  val ecc_b: BigInteger
  val ecc_n: BigInteger
  val ecc_gx: BigInteger
  val ecc_gy: BigInteger
  val ecc_curve: ECCurve
  val ecc_point_g: ECPoint
  val ecc_bc_spec: ECDomainParameters
  val ecc_key_pair_generator: ECKeyPairGenerator
  val ecc_gx_fieldelement: ECFieldElement
  val ecc_gy_fieldelement: ECFieldElement

  companion object {
    //测试参数
    //	public static final String[] ecc_param = {
    //	    "8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3",
    //	    "787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498",
    //	    "63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A",
    //	    "8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7",
    //	    "421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D",
    //	    "0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2"
    //	};
    //正式参数
    var ecc_param = arrayOf(
      "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
      "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
      "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
      "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
      "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
      "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0"
    )

    fun Instance(): SM2 {
      return SM2()
    }
  }

  init {
    ecc_p = BigInteger(ecc_param[0], 16)
    ecc_a = BigInteger(ecc_param[1], 16)
    ecc_b = BigInteger(ecc_param[2], 16)
    ecc_n = BigInteger(ecc_param[3], 16)
    ecc_gx = BigInteger(ecc_param[4], 16)
    ecc_gy = BigInteger(ecc_param[5], 16)
    ecc_gx_fieldelement = ECFieldElement.Fp(ecc_p, ecc_gx)
    ecc_gy_fieldelement = ECFieldElement.Fp(ecc_p, ecc_gy)
    ecc_curve = ECCurve.Fp(ecc_p, ecc_a, ecc_b)
    ecc_point_g = ECPoint.Fp(ecc_curve, ecc_gx_fieldelement, ecc_gy_fieldelement)
    ecc_bc_spec = ECDomainParameters(ecc_curve, ecc_point_g, ecc_n)
    ecc_key_pair_generator = ECKeyPairGenerator()
    ecc_key_pair_generator.init(ECKeyGenerationParameters(ecc_bc_spec, SecureRandom()))
  }
}
package com.cioinfotech.cychat.features.cycore

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES {

    private const val cipherTransformation = "AES/CBC/PKCS5Padding"
    private const val aesEncryptionAlgorithm = "AES"
    private var ivBytes = byteArrayOf(
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00
    )
    private lateinit var keyBytes: ByteArray

//    @Throws(Exception::class)
//    fun encrypt_string(plain: String): String {
//        return encrypt(plain.toByteArray())
//    }

//    @Throws(Exception::class, NoSuchAlgorithmException::class)
//    fun decrypt_string(plain: String): String {
//        return decrypt(Base64.decode(plain, Base64.DEFAULT))
//    }

    @Throws(Exception::class)
    fun encrypt(text: String, key: String): String {
        val mes = text.toByteArray()
        keyBytes = key.toByteArray(charset("UTF-8"))
        val md = MessageDigest.getInstance("SHA-256")
        md.update(keyBytes)
        keyBytes = md.digest()

//		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        val newKey = SecretKeySpec(keyBytes, aesEncryptionAlgorithm)
        val cipher = Cipher.getInstance(cipherTransformation)
        val random = SecureRandom()
        ivBytes = ByteArray(16)
        random.nextBytes(ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, newKey, random)
        //    cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        val destination = ByteArray(ivBytes.size + mes.size)
        System.arraycopy(ivBytes, 0, destination, 0, ivBytes.size)
        System.arraycopy(mes, 0, destination, ivBytes.size, mes.size)
        return Base64.encodeToString(cipher.doFinal(destination), Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decrypt(text: String, key: String): String {
        val bytes = Base64.decode(text, Base64.DEFAULT)
        keyBytes = key.toByteArray(charset("UTF-8"))
        val md = MessageDigest.getInstance("SHA-256")
        md.update(keyBytes)
        keyBytes = md.digest()
        val ivB = bytes.copyOfRange(0, 16)
        val codB = bytes.copyOfRange(16, bytes.size)
        val ivSpec: AlgorithmParameterSpec = IvParameterSpec(ivB)
        val newKey =
                SecretKeySpec(keyBytes, aesEncryptionAlgorithm)
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec)
        return String(cipher.doFinal(codB))
    }

    fun createSecretKey(userId: String, email: String) = "$userId,$email"
}

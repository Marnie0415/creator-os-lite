package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts sensitive values (API keys) using Android Keystore.
 * No external dependencies required — uses platform crypto APIs.
 *
 * Falls back to plaintext on devices that don't support Keystore (API < 23).
 */
object CryptoManager {
    private const val KEYSTORE_ALIAS = "creator_os_key_encryption"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128 // bits

    private fun getOrCreateSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null

                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Encrypt plaintext. Returns Base64-encoded "IV:cipherext" or original text if encryption fails.
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            val secretKey = getOrCreateSecretKey() ?: return plaintext
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
            "$ivBase64:$cipherBase64"
        } catch (e: Exception) {
            // Fallback to plaintext if encryption fails
            plaintext
        }
    }

    /**
     * Decrypt a Base64-encoded "IV:cipherext" string. Returns original text on failure.
     */
    fun decrypt(encrypted: String): String {
        if (encrypted.isEmpty() || !encrypted.contains(":")) return encrypted
        return try {
            val secretKey = getOrCreateSecretKey() ?: return encrypted
            val parts = encrypted.split(":", limit = 2)
            if (parts.size != 2) return encrypted
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            // If decryption fails, return as-is (might be unencrypted legacy value)
            encrypted
        }
    }
}

package com.example.oidc.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.oidc.model.TokenSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android implementation of SecureStorage using Android Keystore
 */
class AndroidSecureStorage(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : SecureStorage {
    
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    
    private val keyAlias = "oidc_token_key"
    private val tokenKey = "stored_tokens"
    private val sharedPreferences by lazy {
        context.getSharedPreferences("oidc_tokens", Context.MODE_PRIVATE)
    }
    
    override suspend fun saveTokens(tokens: TokenSet) {
        withContext(Dispatchers.IO) {
            val jsonString = json.encodeToString(tokens)
            val encryptedData = encrypt(jsonString)
            sharedPreferences.edit()
                .putString(tokenKey, encryptedData)
                .apply()
        }
    }
    
    override suspend fun getTokens(): TokenSet? {
        return withContext(Dispatchers.IO) {
            val encryptedData = sharedPreferences.getString(tokenKey, null) ?: return@withContext null
            try {
                val jsonString = decrypt(encryptedData)
                json.decodeFromString<TokenSet>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(tokenKey).apply()
        }
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenSpec)
            return keyGenerator.generateKey()
        }
        
        return keyStore.getKey(keyAlias, null) as SecretKey
    }
    
    private fun encrypt(plaintext: String): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        
        // Combine IV and encrypted data
        val result = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, result, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(result, Base64.DEFAULT)
    }
    
    private fun decrypt(encryptedData: String): String {
        val secretKey = getOrCreateSecretKey()
        val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
        
        // Extract IV
        val ivSize = 12 // GCM default IV size
        val iv = decoded.copyOfRange(0, ivSize)
        val encrypted = decoded.copyOfRange(ivSize, decoded.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}
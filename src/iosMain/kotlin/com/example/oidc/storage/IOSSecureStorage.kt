package com.example.oidc.storage

import com.example.oidc.model.TokenSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*
import platform.Security.*

/**
 * iOS implementation of SecureStorage using Keychain Services
 */
class IOSSecureStorage(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : SecureStorage {
    
    private val serviceName = "com.example.oidc"
    private val tokenKey = "stored_tokens"
    
    override suspend fun saveTokens(tokens: TokenSet) {
        withContext(Dispatchers.Default) {
            val jsonString = json.encodeToString(tokens)
            val nsData = jsonString.encodeToByteArray().toNSData()
            
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to tokenKey,
                kSecValueData to nsData,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            )
            
            // First check if item exists
            val existsQuery = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to tokenKey,
                kSecReturnData to kCFBooleanFalse
            )
            
            var status = SecItemCopyMatching(existsQuery.toCFDictionary(), null)
            
            if (status == errSecSuccess) {
                // Item exists, update it
                val updateQuery = mutableMapOf<Any?, Any?>(
                    kSecValueData to nsData
                )
                SecItemUpdate(existsQuery.toCFDictionary(), updateQuery.toCFDictionary())
            } else {
                // Item doesn't exist, add it
                SecItemAdd(query.toCFDictionary(), null)
            }
        }
    }
    
    override suspend fun getTokens(): TokenSet? {
        return withContext(Dispatchers.Default) {
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to tokenKey,
                kSecReturnData to kCFBooleanTrue,
                kSecMatchLimit to kSecMatchLimitOne
            )
            
            val dataRef = NSMutablePointer<CFTypeRef?>()
            val status = SecItemCopyMatching(query.toCFDictionary(), dataRef)
            
            if (status == errSecSuccess && dataRef.value != null) {
                val nsData = dataRef.value as NSData
                val bytes = ByteArray(nsData.length.toInt())
                nsData.getBytes(bytes)
                
                try {
                    val jsonString = bytes.decodeToString()
                    json.decodeFromString<TokenSet>(jsonString)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
    
    override suspend fun clearTokens() {
        withContext(Dispatchers.Default) {
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to tokenKey
            )
            
            SecItemDelete(query.toCFDictionary())
        }
    }
    
    // Helper functions for iOS
    private fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef {
        val keys = this.keys.toNSArray()
        val values = this.values.toNSArray()
        return CFDictionaryCreate(
            null,
            keys,
            values,
            keys.count,
            null,
            null
        )
    }
    
    private fun Collection<Any?>.toNSArray(): NSArray {
        return NSMutableArray().apply {
            this@toNSArray.forEach { addObject(it) }
        }
    }
    
    private fun ByteArray.toNSData(): NSData {
        return NSData.create(bytes = this, length = size.toULong())
    }
}
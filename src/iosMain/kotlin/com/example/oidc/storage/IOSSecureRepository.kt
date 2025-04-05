package com.example.oidc.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.Foundation.*
import platform.Security.*

/**
 * iOS implementation of secure token repository
 */
class IOSSecureRepository(
    json: Json = Json { ignoreUnknownKeys = true }
) : SecureTokenRepository(json) {
    
    private val serviceName = "com.example.oidc"
    
    override suspend fun saveSecurely(key: String, data: String) {
        withContext(Dispatchers.Default) {
            val nsData = data.encodeToByteArray().toNSData()
            
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to key,
                kSecValueData to nsData,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            )
            
            // First check if item exists
            val existsQuery = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to key,
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
    
    override suspend fun getSecurely(key: String): String? {
        return withContext(Dispatchers.Default) {
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to key,
                kSecReturnData to kCFBooleanTrue,
                kSecMatchLimit to kSecMatchLimitOne
            )
            
            val dataRef = NSMutablePointer<CFTypeRef?>()
            val status = SecItemCopyMatching(query.toCFDictionary(), dataRef)
            
            if (status == errSecSuccess && dataRef.value != null) {
                val nsData = dataRef.value as NSData
                val bytes = ByteArray(nsData.length.toInt())
                nsData.getBytes(bytes)
                bytes.decodeToString()
            } else {
                null
            }
        }
    }
    
    override suspend fun removeSecurely(key: String) {
        withContext(Dispatchers.Default) {
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecAttrAccount to key
            )
            
            SecItemDelete(query.toCFDictionary())
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.Default) {
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to serviceName,
                kSecReturnAttributes to kCFBooleanTrue,
                kSecMatchLimit to kSecMatchLimitAll
            )
            
            val resultsRef = NSMutablePointer<CFTypeRef?>()
            val status = SecItemCopyMatching(query.toCFDictionary(), resultsRef)
            
            if (status == errSecSuccess && resultsRef.value != null) {
                val results = resultsRef.value as NSArray
                val keys = mutableListOf<String>()
                
                for (i in 0 until results.count.toInt()) {
                    val item = results.objectAtIndex(i.toULong()) as NSDictionary
                    val account = item.objectForKey(kSecAttrAccount) as NSString
                    keys.add(account.toString())
                }
                
                keys
            } else {
                emptyList()
            }
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
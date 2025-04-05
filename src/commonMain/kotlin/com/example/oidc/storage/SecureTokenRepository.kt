package com.example.oidc.storage

import com.example.oidc.model.TokenScope
import com.example.oidc.model.TokenSet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Base class for secure token storage implementations
 */
abstract class SecureTokenRepository(
    protected val json: Json = Json { ignoreUnknownKeys = true }
) : ScopedTokenRepository {
    
    /**
     * Save data securely
     * @param key Storage key
     * @param data Data to store
     */
    protected abstract suspend fun saveSecurely(key: String, data: String)
    
    /**
     * Get data securely
     * @param key Storage key
     * @return Retrieved data or null if not found
     */
    protected abstract suspend fun getSecurely(key: String): String?
    
    /**
     * Remove data
     * @param key Storage key
     */
    protected abstract suspend fun removeSecurely(key: String)
    
    /**
     * Get all keys
     * @return List of storage keys
     */
    protected abstract suspend fun getAllKeys(): List<String>
    
    override suspend fun saveTokens(scope: TokenScope, tokens: TokenSet) {
        val key = scope.toStorageKey()
        val jsonString = json.encodeToString(tokens)
        saveSecurely(key, jsonString)
    }
    
    override suspend fun getTokens(scope: TokenScope): TokenSet? {
        val key = scope.toStorageKey()
        val jsonString = getSecurely(key) ?: return null
        
        return try {
            json.decodeFromString<TokenSet>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAllScopes(): List<TokenScope> {
        return getAllKeys()
            .filter { it.startsWith("client_") }
            .mapNotNull { parseStorageKey(it) }
    }
    
    override suspend fun clearTokens(scope: TokenScope) {
        val key = scope.toStorageKey()
        removeSecurely(key)
    }
    
    override suspend fun clearAllTokens() {
        getAllKeys()
            .filter { it.startsWith("client_") }
            .forEach { removeSecurely(it) }
    }
    
    /**
     * Parse a storage key into a TokenScope
     * @param storageKey The storage key
     * @return TokenScope or null if parsing fails
     */
    protected fun parseStorageKey(storageKey: String): TokenScope? {
        // Simple parser - can be improved
        val parts = storageKey.split("_")
        if (parts.size < 2 || parts[0] != "client") return null
        
        var clientId = parts[1]
        var userId: String? = null
        var scope: String? = null
        var resource: String? = null
        var purpose: String? = null
        
        var i = 2
        while (i < parts.size - 1) {
            when (parts[i]) {
                "user" -> userId = parts[i + 1]
                "scope" -> scope = parts[i + 1].replace("_", " ")
                "resource" -> resource = parts[i + 1]
                "purpose" -> purpose = parts[i + 1]
            }
            i += 2
        }
        
        return TokenScope(
            clientId = clientId,
            userId = userId,
            scope = scope,
            resource = resource,
            purpose = purpose
        )
    }
}
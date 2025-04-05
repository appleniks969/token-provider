package com.example.oidc.storage

import com.example.oidc.model.TokenScope
import com.example.oidc.model.TokenSet
import kotlinx.serialization.json.Json

/**
 * In-memory implementation of ScopedTokenRepository (for testing)
 */
class InMemoryTokenRepository(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ScopedTokenRepository {
    private val tokenStore = mutableMapOf<String, TokenSet>()
    
    override suspend fun saveTokens(scope: TokenScope, tokens: TokenSet) {
        val key = scope.toStorageKey()
        tokenStore[key] = tokens
    }
    
    override suspend fun getTokens(scope: TokenScope): TokenSet? {
        val key = scope.toStorageKey()
        return tokenStore[key]
    }
    
    override suspend fun getAllScopes(): List<TokenScope> {
        // This is a simplistic implementation - in reality, you'd store the
        // actual TokenScope objects separately or parse them from the keys
        return tokenStore.keys.map { key ->
            // Simplified parser - real implementation would be more robust
            TokenScope(clientId = key.removePrefix("client_"))
        }
    }
    
    override suspend fun clearTokens(scope: TokenScope) {
        val key = scope.toStorageKey()
        tokenStore.remove(key)
    }
    
    override suspend fun clearAllTokens() {
        tokenStore.clear()
    }
}
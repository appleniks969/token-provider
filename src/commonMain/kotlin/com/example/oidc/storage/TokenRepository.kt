package com.example.oidc.storage

import com.example.oidc.model.TokenSet

/**
 * Repository for storing and retrieving tokens
 */
interface TokenRepository {
    /**
     * Save tokens
     * @param tokens The tokens to store
     */
    suspend fun saveTokens(tokens: TokenSet)
    
    /**
     * Get stored tokens
     * @return The stored tokens or null if not found
     */
    suspend fun getTokens(): TokenSet?
    
    /**
     * Clear stored tokens
     */
    suspend fun clearTokens()
}
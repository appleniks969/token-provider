package com.example.oidc.storage

import com.example.oidc.model.TokenScope
import com.example.oidc.model.TokenSet

/**
 * Repository for storing and retrieving tokens with scope support
 */
interface ScopedTokenRepository : TokenRepository {
    /**
     * Save tokens for a specific scope
     * @param scope The token scope
     * @param tokens The tokens to store
     */
    suspend fun saveTokens(scope: TokenScope, tokens: TokenSet)
    
    /**
     * Get tokens for a specific scope
     * @param scope The token scope
     * @return The stored tokens or null if not found
     */
    suspend fun getTokens(scope: TokenScope): TokenSet?
    
    /**
     * Get all available token scopes
     * @return List of available token scopes
     */
    suspend fun getAllScopes(): List<TokenScope>
    
    /**
     * Clear tokens for a specific scope
     * @param scope The token scope
     */
    suspend fun clearTokens(scope: TokenScope)
    
    /**
     * Clear all stored tokens
     */
    suspend fun clearAllTokens()
    
    /**
     * Default implementation for TokenRepository interface methods
     * Uses the default scope
     */
    override suspend fun saveTokens(tokens: TokenSet) {
        saveTokens(TokenScope.DEFAULT, tokens)
    }
    
    /**
     * Default implementation for TokenRepository interface methods
     * Uses the default scope
     */
    override suspend fun getTokens(): TokenSet? {
        return getTokens(TokenScope.DEFAULT)
    }
    
    /**
     * Default implementation for TokenRepository interface methods
     * Uses the default scope
     */
    override suspend fun clearTokens() {
        clearTokens(TokenScope.DEFAULT)
    }
}
package com.example.oidc

import com.example.oidc.model.*
import com.example.oidc.client.TokenClient
import com.example.oidc.storage.TokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * TokenProvider - Main entry point for OIDC token management
 * 
 * Provides operations for discovering OIDC configuration, obtaining tokens,
 * and managing token lifecycle.
 */
class TokenProvider(
    private val client: TokenClient,
    private val repository: TokenRepository,
    private val coroutineScope: CoroutineScope
) {
    // Cache for the OIDC configuration
    private var cachedConfig: TokenEndpoints? = null
    
    // Current token state
    val tokenState: StateFlow<TokenState> = client.tokenState.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = TokenState.NoToken
    )
    
    /**
     * Discover OIDC provider configuration
     * 
     * @param issuerUrl Base URL of the OIDC provider
     * @return Result containing discovered endpoints or error
     */
    suspend fun discoverConfiguration(issuerUrl: String): TokenResult<TokenEndpoints> {
        return withContext(Dispatchers.IO) {
            val result = client.discoverConfiguration(issuerUrl)
            
            if (result is TokenResult.Success) {
                cachedConfig = result.data
                
                // Check if we have stored tokens
                val storedTokens = repository.getTokens()
                if (storedTokens != null && !storedTokens.isExpired()) {
                    (client.tokenState as? MutableStateFlow)?.value = TokenState.Valid(storedTokens)
                }
            }
            
            result
        }
    }
    
    /**
     * Get a valid access token, refreshing if necessary
     * 
     * @param clientId The client ID
     * @param clientSecret Optional client secret
     * @param force Force refresh even if token isn't expired
     * @return Result containing access token or error
     */
    suspend fun getAccessToken(
        clientId: String,
        clientSecret: String? = null,
        force: Boolean = false
    ): TokenResult<String> {
        return withContext(Dispatchers.IO) {
            val config = cachedConfig ?: return@withContext TokenResult.Error(
                Exception("Provider not initialized. Call discoverConfiguration() first.")
            )
            
            val storedTokens = repository.getTokens()
            
            if (storedTokens == null) {
                return@withContext TokenResult.Error(
                    Exception("No tokens available.")
                )
            }
            
            // Check if we need to refresh
            if (force || storedTokens.isExpired()) {
                val refreshToken = storedTokens.refreshToken ?: return@withContext TokenResult.Error(
                    Exception("No refresh token available.")
                )
                
                val refreshResult = refreshAccessToken(
                    clientId = clientId,
                    refreshToken = refreshToken,
                    clientSecret = clientSecret,
                    scope = storedTokens.scope
                )
                
                when (refreshResult) {
                    is TokenResult.Success -> {
                        return@withContext TokenResult.Success(refreshResult.data.accessToken)
                    }
                    is TokenResult.Error -> {
                        return@withContext refreshResult
                    }
                }
            }
            
            // Return existing token
            TokenResult.Success(storedTokens.accessToken)
        }
    }
    
    /**
     * Refresh an access token using a refresh token
     * 
     * @param clientId The client ID
     * @param refreshToken The refresh token
     * @param clientSecret Optional client secret
     * @param scope Optional scope
     * @return Result containing new token set or error
     */
    suspend fun refreshAccessToken(
        clientId: String,
        refreshToken: String,
        clientSecret: String? = null,
        scope: String? = null
    ): TokenResult<TokenSet> {
        return withContext(Dispatchers.IO) {
            val config = cachedConfig ?: return@withContext TokenResult.Error(
                Exception("Provider not initialized. Call discoverConfiguration() first.")
            )
            
            val result = client.refreshToken(
                tokenEndpoint = config.tokenEndpoint,
                clientId = clientId,
                refreshToken = refreshToken,
                clientSecret = clientSecret,
                scope = scope
            )
            
            if (result is TokenResult.Success) {
                repository.saveTokens(result.data)
            }
            
            result
        }
    }
    
    /**
     * Get a stored auto login code if available
     * 
     * @return The auto login code or null if not available
     */
    suspend fun getAutoLoginCode(): String? {
        return withContext(Dispatchers.IO) {
            repository.getTokens()?.autoLoginCode
        }
    }
    
    /**
     * Request a new auto login code from the OIDC provider
     * 
     * @param clientId The client ID
     * @param username The username for which to generate the auto login code
     * @param clientSecret Optional client secret
     * @param additionalParams Additional parameters required by the provider
     * @return Result containing the auto login code or error
     */
    suspend fun requestAutoLoginCode(
        clientId: String,
        username: String,
        clientSecret: String? = null,
        additionalParams: Map<String, String> = emptyMap()
    ): TokenResult<String> {
        return withContext(Dispatchers.IO) {
            val config = cachedConfig ?: return@withContext TokenResult.Error(
                Exception("Provider not initialized. Call discoverConfiguration() first.")
            )
            
            val result = client.requestAutoLoginCode(
                tokenEndpoint = config.tokenEndpoint,
                clientId = clientId,
                username = username,
                clientSecret = clientSecret,
                additionalParams = additionalParams
            )
            
            if (result is TokenResult.Success) {
                // Update stored tokens with the new auto login code if we have existing tokens
                repository.getTokens()?.let { tokens ->
                    val updatedTokens = tokens.copy(autoLoginCode = result.data)
                    repository.saveTokens(updatedTokens)
                }
            }
            
            result
        }
    }
    
    companion object {
        /**
         * Create a TokenProvider with the given dependencies
         */
        fun create(
            client: TokenClient,
            repository: TokenRepository,
            coroutineScope: CoroutineScope
        ): TokenProvider {
            return TokenProvider(client, repository, coroutineScope)
        }
    }
}
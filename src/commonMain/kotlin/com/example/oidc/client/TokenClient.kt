package com.example.oidc.client

import com.example.oidc.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Client for interacting with OIDC token endpoints
 */
interface TokenClient {
    /**
     * Current token state
     */
    val tokenState: Flow<TokenState>
    
    /**
     * Discover OIDC configuration from the well-known endpoint
     * 
     * @param issuerUrl Base URL of the OIDC provider
     * @return Result containing discovered endpoints or error
     */
    suspend fun discoverConfiguration(issuerUrl: String): TokenResult<TokenEndpoints>
    
    /**
     * Refresh an access token using a refresh token
     * 
     * @param tokenEndpoint URL of the token endpoint
     * @param clientId The client ID
     * @param refreshToken The refresh token
     * @param clientSecret Optional client secret
     * @param scope Optional scope
     * @return Result containing new token set or error
     */
    suspend fun refreshToken(
        tokenEndpoint: String,
        clientId: String,
        refreshToken: String,
        clientSecret: String? = null,
        scope: String? = null
    ): TokenResult<TokenSet>
    
    /**
     * Request an auto login code from the OIDC provider
     * 
     * @param tokenEndpoint URL of the token endpoint
     * @param clientId The client ID
     * @param username The username for which to generate the auto login code
     * @param clientSecret Optional client secret
     * @param additionalParams Additional parameters required by the provider
     * @return Result containing the auto login code or error
     */
    suspend fun requestAutoLoginCode(
        tokenEndpoint: String,
        clientId: String,
        username: String,
        clientSecret: String? = null,
        additionalParams: Map<String, String> = emptyMap()
    ): TokenResult<String>
}
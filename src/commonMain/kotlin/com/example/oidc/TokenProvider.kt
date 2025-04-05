package com.example.oidc

import com.example.oidc.model.*
import com.example.oidc.storage.SecureStorage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * The main entry point for OIDC token management.
 *
 * This class provides a simplified API for obtaining access tokens from an OIDC provider,
 * handling discovery, token acquisition, and refreshing automatically.
 */
class TokenProvider(
    private val httpClient: HttpClient,
    private val storage: SecureStorage,
    private val coroutineScope: CoroutineScope,
    private val issuerUrl: String,
    private val clientId: String,
    private val clientSecret: String? = null
) {
    // Cache for the OIDC configuration
    private var cachedConfig: TokenEndpoints? = null
    
    // Current token state flow
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.NoToken)
    val tokenState: StateFlow<TokenState> = _tokenState.asStateFlow()
    
    // Flag to track if discovery has been attempted
    private var discoveryAttempted = false
    
    /**
     * Get a valid access token, automatically handling discovery and refresh if needed.
     * 
     * This is the main method clients should use. It handles all the complexity of
     * token management internally.
     * 
     * @param forceRefresh Whether to force refresh even if token isn't expired
     * @return Result containing both access and refresh tokens, or an error
     */
    suspend fun getAccessToken(forceRefresh: Boolean = false): TokenResult<TokenSet> {
        return withContext(Dispatchers.IO) {
            // Ensure we have discovered the OIDC configuration
            if (cachedConfig == null) {
                val discoveryResult = discoverConfiguration()
                if (discoveryResult is TokenResult.Error) {
                    return@withContext TokenResult.Error(Exception("Failed to discover OIDC configuration: ${discoveryResult.exception.message}"))
                }
            }
            
            // The configuration should now be available
            val config = cachedConfig ?: return@withContext TokenResult.Error(
                Exception("OIDC configuration not available after discovery")
            )
            
            // Check if we have stored tokens
            val storedTokens = storage.getTokens()
            
            // If we have tokens and they're not expired (and no force refresh), return them
            if (storedTokens != null && !storedTokens.isExpired() && !forceRefresh) {
                _tokenState.value = TokenState.Valid(storedTokens)
                return@withContext TokenResult.Success(storedTokens)
            }
            
            // If we have a refresh token, try to refresh
            if (storedTokens?.refreshToken != null) {
                val refreshResult = refreshAccessToken(config, storedTokens.refreshToken)
                
                if (refreshResult is TokenResult.Success) {
                    return@withContext refreshResult
                }
                
                // If refresh failed, we'll try to get a new token (if refresh token is required by the provider,
                // this will fail appropriately)
            }
            
            // If we reach here, we couldn't get a token via refresh, so we'll return an error
            // In a real implementation, you might redirect to a login page at this point
            return@withContext TokenResult.Error(Exception("No valid tokens available and automatic refresh failed"))
        }
    }
    
    /**
     * Get a stored auto login code if available
     * 
     * @return The auto login code or null if not available
     */
    suspend fun getAutoLoginCode(): String? {
        return withContext(Dispatchers.IO) {
            storage.getTokens()?.autoLoginCode
        }
    }
    
    /**
     * Request a new auto login code from the OIDC provider
     * 
     * @param username The username for which to generate the auto login code
     * @param additionalParams Additional parameters required by the provider
     * @return Result containing the auto login code or error
     */
    suspend fun requestAutoLoginCode(
        username: String,
        additionalParams: Map<String, String> = emptyMap()
    ): TokenResult<String> {
        return withContext(Dispatchers.IO) {
            // Ensure we have discovered the OIDC configuration
            if (cachedConfig == null) {
                val discoveryResult = discoverConfiguration()
                if (discoveryResult is TokenResult.Error) {
                    return@withContext TokenResult.Error(Exception("Failed to discover OIDC configuration: ${discoveryResult.exception.message}"))
                }
            }
            
            val config = cachedConfig ?: return@withContext TokenResult.Error(
                Exception("OIDC configuration not available after discovery")
            )
            
            try {
                val parameters = Parameters.build {
                    append("client_id", clientId)
                    clientSecret?.let { append("client_secret", it) }
                    append("username", username)
                    append("grant_type", "auto_login_code")
                    
                    // Add any additional parameters
                    additionalParams.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                
                val response = httpClient.submitForm(
                    url = config.tokenEndpoint,
                    formParameters = parameters
                )
                
                if (response.status.isSuccess()) {
                    val tokenResponse: TokenResponse = response.body()
                    tokenResponse.autoLoginCode?.let {
                        // Update stored tokens with the new auto login code if we have existing tokens
                        storage.getTokens()?.let { tokens ->
                            val updatedTokens = tokens.copy(autoLoginCode = it)
                            storage.saveTokens(updatedTokens)
                        }
                        return@withContext TokenResult.Success(it)
                    } ?: return@withContext TokenResult.Error(Exception("No auto login code returned"))
                } else {
                    val error: TokenError = response.body()
                    return@withContext TokenResult.Error(Exception(error.errorDescription ?: error.error))
                }
            } catch (e: Exception) {
                return@withContext TokenResult.Error(e)
            }
        }
    }
    
    /**
     * Internal method to discover OIDC configuration.
     * This is called automatically by getAccessToken when needed.
     */
    private suspend fun discoverConfiguration(): TokenResult<TokenEndpoints> {
        if (discoveryAttempted && cachedConfig != null) {
            return TokenResult.Success(cachedConfig!!)
        }
        
        discoveryAttempted = true
        
        return try {
            val wellKnownUrl = if (issuerUrl.endsWith("/")) {
                "${issuerUrl}.well-known/openid-configuration"
            } else {
                "$issuerUrl/.well-known/openid-configuration"
            }
            
            val response: TokenEndpoints = httpClient.get(wellKnownUrl).body()
            cachedConfig = response
            
            // Check if we have stored tokens
            val storedTokens = storage.getTokens()
            if (storedTokens != null && !storedTokens.isExpired()) {
                _tokenState.value = TokenState.Valid(storedTokens)
            }
            
            TokenResult.Success(response)
        } catch (e: Exception) {
            TokenResult.Error(e)
        }
    }
    
    /**
     * Internal method to refresh an access token using a refresh token.
     */
    private suspend fun refreshAccessToken(
        config: TokenEndpoints,
        refreshToken: String
    ): TokenResult<TokenSet> {
        return try {
            _tokenState.value = TokenState.Refreshing
            
            val parameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", clientId)
                append("refresh_token", refreshToken)
                clientSecret?.let { append("client_secret", it) }
            }
            
            val response = httpClient.submitForm(
                url = config.tokenEndpoint,
                formParameters = parameters
            )
            
            if (response.status.isSuccess()) {
                val tokenResponse: TokenResponse = response.body()
                val tokenSet = TokenSet.fromResponse(tokenResponse)
                storage.saveTokens(tokenSet)
                _tokenState.value = TokenState.Valid(tokenSet)
                TokenResult.Success(tokenSet)
            } else {
                val error: TokenError = response.body()
                _tokenState.value = TokenState.Invalid(error.errorDescription ?: error.error)
                TokenResult.Error(Exception(error.errorDescription ?: error.error))
            }
        } catch (e: Exception) {
            _tokenState.value = TokenState.Invalid(e.message ?: "Unknown error")
            TokenResult.Error(e)
        }
    }
    
    companion object {
        /**
         * Create a TokenProvider with the given dependencies and configuration
         * 
         * @param engine HTTP engine to use
         * @param storage Secure storage implementation
         * @param coroutineScope Coroutine scope for async operations
         * @param issuerUrl Base URL of the OIDC provider
         * @param clientId Client ID for the OIDC provider
         * @param clientSecret Optional client secret
         */
        fun create(
            engine: HttpClientEngine,
            storage: SecureStorage,
            coroutineScope: CoroutineScope,
            issuerUrl: String,
            clientId: String,
            clientSecret: String? = null
        ): TokenProvider {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            }
            
            val httpClient = HttpClient(engine) {
                install(ContentNegotiation) {
                    json(json)
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                    connectTimeoutMillis = 15000
                    socketTimeoutMillis = 15000
                }
                defaultRequest {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }
            }
            
            return TokenProvider(
                httpClient = httpClient,
                storage = storage,
                coroutineScope = coroutineScope,
                issuerUrl = issuerUrl,
                clientId = clientId,
                clientSecret = clientSecret
            )
        }
    }
}
package com.example.oidc.examples

import com.example.oidc.TokenProvider
import com.example.oidc.client.KtorTokenClient
import com.example.oidc.model.TokenResult
import com.example.oidc.model.TokenScope
import com.example.oidc.storage.ScopedTokenRepository
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Example of using the Token Provider
 */
class TokenExample(
    private val repository: ScopedTokenRepository,
    private val engine: HttpClientEngine
) {
    // Create a coroutine scope for async operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Create the token client
    private val client = KtorTokenClient.create(engine)
    
    // Create the token provider
    private val provider = TokenProvider.create(
        client = client,
        repository = repository,
        coroutineScope = scope
    )
    
    // OIDC provider URL
    private val issuerUrl = "https://example.auth0.com"
    
    /**
     * Initialize the token provider
     */
    fun initialize(callback: (Boolean) -> Unit) {
        scope.launch {
            val result = provider.discoverConfiguration(issuerUrl)
            
            when (result) {
                is TokenResult.Success -> {
                    println("Discovered OIDC configuration")
                    println("Token endpoint: ${result.data.tokenEndpoint}")
                    callback(true)
                }
                is TokenResult.Error -> {
                    println("Failed to discover configuration: ${result.exception.message}")
                    callback(false)
                }
            }
        }
    }
    
    /**
     * Use an access token for API calls
     */
    fun callApiWithToken(apiUrl: String, callback: (String?) -> Unit) {
        scope.launch {
            when (val result = provider.getAccessToken(
                clientId = "your-client-id",
                clientSecret = "your-client-secret" // Optional
            )) {
                is TokenResult.Success -> {
                    val accessToken = result.data
                    println("Got access token: ${accessToken.take(10)}...")
                    
                    // Make API call with access token
                    // This would actually use Ktor to make the API call
                    val apiResponse = simulateApiCall(apiUrl, accessToken)
                    callback(apiResponse)
                }
                is TokenResult.Error -> {
                    println("Failed to get access token: ${result.exception.message}")
                    callback(null)
                }
            }
        }
    }
    
    /**
     * Get an auto login code
     */
    fun getAutoLoginCode(username: String, callback: (String?) -> Unit) {
        scope.launch {
            // First check if we already have one
            val existingCode = provider.getAutoLoginCode()
            if (existingCode != null) {
                println("Found existing auto login code")
                callback(existingCode)
                return@launch
            }
            
            // Request a new one
            when (val result = provider.requestAutoLoginCode(
                clientId = "your-client-id",
                username = username,
                clientSecret = "your-client-secret", // Optional
                additionalParams = mapOf(
                    "redirect_uri" to "myapp://callback"
                )
            )) {
                is TokenResult.Success -> {
                    val code = result.data
                    println("Got auto login code: $code")
                    callback(code)
                }
                is TokenResult.Error -> {
                    println("Failed to get auto login code: ${result.exception.message}")
                    callback(null)
                }
            }
        }
    }
    
    /**
     * Example of working with multiple scopes
     */
    fun workWithMultipleScopes() {
        // This only works if using a ScopedTokenRepository
        if (repository !is ScopedTokenRepository) return
        
        scope.launch {
            // Get all available scopes
            val scopes = repository.getAllScopes()
            println("Available scopes: $scopes")
            
            // Use tokens for a specific scope
            val readScope = TokenScope.forScope(
                clientId = "your-client-id",
                scope = "read",
                userId = "user123"
            )
            
            val readTokens = repository.getTokens(readScope)
            if (readTokens != null) {
                println("Using read scope token: ${readTokens.accessToken.take(10)}...")
                // Use the token for read operations
            }
            
            // Use tokens for another scope
            val writeScope = TokenScope.forScope(
                clientId = "your-client-id",
                scope = "write",
                userId = "user123"
            )
            
            val writeTokens = repository.getTokens(writeScope)
            if (writeTokens != null) {
                println("Using write scope token: ${writeTokens.accessToken.take(10)}...")
                // Use the token for write operations
            }
        }
    }
    
    /**
     * Simulate an API call with an access token
     * In a real app, this would use Ktor to make the actual HTTP request
     */
    private fun simulateApiCall(url: String, accessToken: String): String {
        // This is just a placeholder - in a real app, you would use Ktor to make the API call
        return "Response from $url"
    }
}
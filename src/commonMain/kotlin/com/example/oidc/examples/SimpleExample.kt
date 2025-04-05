package com.example.oidc.examples

import com.example.oidc.TokenProvider
import com.example.oidc.model.TokenResult
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Simple example showing how to use the simplified TokenProvider API
 */
class SimpleExample {
    
    // OIDC Provider configuration
    private val issuerUrl = "https://example.auth0.com"
    private val clientId = "your-client-id"
    private val clientSecret = "your-client-secret" // Optional
    
    // The TokenProvider
    private lateinit var tokenProvider: TokenProvider
    
    // A coroutine scope for async operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Example of initializing the TokenProvider
     */
    fun initialize(engine: HttpClientEngine, storage: com.example.oidc.storage.SecureStorage) {
        // Create the TokenProvider with all required dependencies
        tokenProvider = TokenProvider.create(
            engine = engine,
            storage = storage,
            coroutineScope = scope,
            issuerUrl = issuerUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )
    }
    
    /**
     * Example of getting an access token
     */
    fun getAccessToken(callback: (String?) -> Unit) {
        scope.launch {
            // Simply call getAccessToken() - it handles discovery and refresh automatically
            when (val result = tokenProvider.getAccessToken()) {
                is TokenResult.Success -> {
                    val tokens = result.data
                    println("Got tokens: Access: ${tokens.accessToken.take(10)}...")
                    
                    // The access token is what you need for API calls
                    callback(tokens.accessToken)
                }
                is TokenResult.Error -> {
                    println("Failed to get tokens: ${result.exception.message}")
                    callback(null)
                }
            }
        }
    }
    
    /**
     * Example of getting an auto login code
     */
    fun getAutoLoginCode(username: String, callback: (String?) -> Unit) {
        scope.launch {
            // First check if we already have one
            val existingCode = tokenProvider.getAutoLoginCode()
            if (existingCode != null) {
                println("Found existing auto login code: $existingCode")
                callback(existingCode)
                return@launch
            }
            
            // Request a new code - no need to worry about discovery
            when (val result = tokenProvider.requestAutoLoginCode(
                username = username,
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
}
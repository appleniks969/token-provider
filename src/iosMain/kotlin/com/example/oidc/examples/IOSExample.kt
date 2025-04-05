package com.example.oidc.examples

import com.example.oidc.TokenProvider
import com.example.oidc.storage.IOSSecureStorage
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Example of creating the TokenProvider on iOS
 */
object IOSExample {
    /**
     * Create the TokenProvider with iOS-specific dependencies
     * 
     * @param issuerUrl Base URL of the OIDC provider
     * @param clientId Client ID for the OIDC provider
     * @param clientSecret Optional client secret
     * @return Configured TokenProvider
     */
    fun createTokenProvider(
        issuerUrl: String,
        clientId: String,
        clientSecret: String? = null
    ): TokenProvider {
        // Create the iOS secure storage
        val storage = IOSSecureStorage()
        
        // Create the HTTP engine
        val engine = Darwin.create()
        
        // Create the coroutine scope
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        
        // Create and return the TokenProvider
        return TokenProvider.create(
            engine = engine,
            storage = storage,
            coroutineScope = coroutineScope,
            issuerUrl = issuerUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )
    }
}
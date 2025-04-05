package com.example.oidc.examples

import com.example.oidc.TokenProvider
import com.example.oidc.client.KtorTokenClient
import com.example.oidc.storage.IOSSecureRepository
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Example of creating and using the TokenProvider on iOS
 */
object IOSExample {
    /**
     * Create the TokenProvider with iOS-specific dependencies
     * 
     * @return Configured TokenProvider
     */
    fun createTokenProvider(): TokenProvider {
        // Create the repository
        val repository = IOSSecureRepository()
        
        // Create the HTTP engine
        val engine = Darwin.create()
        
        // Create the token client
        val client = KtorTokenClient.create(engine)
        
        // Create the coroutine scope
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        
        // Create and return the token provider
        return TokenProvider.create(
            client = client,
            repository = repository,
            coroutineScope = coroutineScope
        )
    }
}
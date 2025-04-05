package com.example.oidc.examples

import android.content.Context
import com.example.oidc.TokenProvider
import com.example.oidc.client.KtorTokenClient
import com.example.oidc.storage.AndroidSecureRepository
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Example of creating and using the TokenProvider on Android
 */
object AndroidExample {
    /**
     * Create the TokenProvider with Android-specific dependencies
     * 
     * @param context Android Context
     * @return Configured TokenProvider
     */
    fun createTokenProvider(context: Context): TokenProvider {
        // Create the repository
        val repository = AndroidSecureRepository(context)
        
        // Create the HTTP engine
        val engine = Android.create()
        
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
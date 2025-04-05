package com.example.oidc.examples

import android.content.Context
import com.example.oidc.TokenProvider
import com.example.oidc.storage.AndroidSecureStorage
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Example of creating the TokenProvider on Android
 */
object AndroidExample {
    /**
     * Create the TokenProvider with Android-specific dependencies
     * 
     * @param context Android Context
     * @param issuerUrl Base URL of the OIDC provider
     * @param clientId Client ID for the OIDC provider
     * @param clientSecret Optional client secret
     * @return Configured TokenProvider
     */
    fun createTokenProvider(
        context: Context,
        issuerUrl: String,
        clientId: String,
        clientSecret: String? = null
    ): TokenProvider {
        // Create the Android secure storage
        val storage = AndroidSecureStorage(context)
        
        // Create the HTTP engine
        val engine = Android.create()
        
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
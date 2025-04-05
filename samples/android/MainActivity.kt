package com.example.oidc.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oidc.TokenProvider
import com.example.oidc.examples.AndroidExample
import com.example.oidc.model.TokenResult
import com.example.oidc.model.TokenState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Sample Android Activity demonstrating the usage of the simplified OIDC Token Provider SDK
 */
class MainActivity : AppCompatActivity() {
    
    // UI components
    private lateinit var statusTextView: TextView
    private lateinit var tokenTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var getTokenButton: Button
    private lateinit var autoLoginButton: Button
    
    // TokenProvider
    private lateinit var tokenProvider: TokenProvider
    
    // OIDC Configuration
    private val issuerUrl = "https://example.auth0.com"
    private val clientId = "your-client-id" // Replace with your client ID
    private val clientSecret = "your-client-secret" // Replace with your client secret or null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI components
        statusTextView = findViewById(R.id.statusTextView)
        tokenTextView = findViewById(R.id.tokenTextView)
        progressBar = findViewById(R.id.progressBar)
        getTokenButton = findViewById(R.id.getTokenButton)
        autoLoginButton = findViewById(R.id.autoLoginButton)
        
        // Initialize TokenProvider using the simplified factory method
        tokenProvider = AndroidExample.createTokenProvider(
            context = applicationContext,
            issuerUrl = issuerUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )
        
        // Set up button click listeners
        setupClickListeners()
        
        // Observe token state
        observeTokenState()
    }
    
    private fun setupClickListeners() {
        // Get access token
        getTokenButton.setOnClickListener {
            getAccessToken()
        }
        
        // Get auto login code
        autoLoginButton.setOnClickListener {
            getAutoLoginCode()
        }
    }
    
    private fun observeTokenState() {
        lifecycleScope.launch {
            tokenProvider.tokenState.collect { state ->
                when (state) {
                    is TokenState.NoToken -> {
                        statusTextView.text = "No tokens available"
                        progressBar.visibility = View.GONE
                    }
                    is TokenState.Refreshing -> {
                        statusTextView.text = "Refreshing tokens..."
                        progressBar.visibility = View.VISIBLE
                    }
                    is TokenState.Valid -> {
                        statusTextView.text = "Token valid until: ${state.tokens.expiresAt}"
                        progressBar.visibility = View.GONE
                        tokenTextView.text = "Access Token: ${state.tokens.accessToken.take(10)}..."
                    }
                    is TokenState.Invalid -> {
                        statusTextView.text = "Token error: ${state.message}"
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    private fun getAccessToken() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = tokenProvider.getAccessToken()) {
                is TokenResult.Success -> {
                    val tokenSet = result.data
                    Log.d(TAG, "Got access token: ${tokenSet.accessToken.take(10)}...")
                    Toast.makeText(
                        this@MainActivity,
                        "Access token obtained successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is TokenResult.Error -> {
                    Log.e(TAG, "Access token error", result.exception)
                    Toast.makeText(
                        this@MainActivity,
                        "Access token error: ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            progressBar.visibility = View.GONE
        }
    }
    
    private fun getAutoLoginCode() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            // First check if we already have one
            val existingCode = tokenProvider.getAutoLoginCode()
            if (existingCode != null) {
                Log.d(TAG, "Found existing auto login code: $existingCode")
                Toast.makeText(
                    this@MainActivity,
                    "Existing auto login code: $existingCode",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                return@launch
            }
            
            // Request a new code
            when (val result = tokenProvider.requestAutoLoginCode(
                username = "user@example.com", // Replace with actual username
                additionalParams = mapOf(
                    "redirect_uri" to "myapp://callback"
                )
            )) {
                is TokenResult.Success -> {
                    val code = result.data
                    Log.d(TAG, "Got auto login code: $code")
                    Toast.makeText(
                        this@MainActivity,
                        "Auto login code: $code",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is TokenResult.Error -> {
                    Log.e(TAG, "Auto login code error", result.exception)
                    Toast.makeText(
                        this@MainActivity,
                        "Auto login code error: ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            progressBar.visibility = View.GONE
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
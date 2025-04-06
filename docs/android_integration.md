# KMM OIDC Token Provider SDK - Android Integration Guide

This guide provides detailed instructions for integrating the KMM OIDC Token Provider SDK into your Android application.

## Setup

### Prerequisites

- Android Studio 4.0 or later
- Minimum Android API level 21 (Android 5.0)
- Kotlin 1.5.0 or later
- Coroutines dependency

### Gradle Configuration

Add the SDK dependency to your app's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.example:oidc:1.0.0")
    
    // Required dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("io.ktor:ktor-client-android:2.2.3")
}
```

## Basic Integration

### Initialize the TokenProvider

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var tokenProvider: TokenProvider
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Create secure storage
        val storage = AndroidSecureStorage(applicationContext)
        
        // Initialize TokenProvider
        tokenProvider = TokenProvider.create(
            engine = Android.create(),
            storage = storage,
            coroutineScope = lifecycleScope,
            issuerUrl = "https://example.auth0.com",
            clientId = "your-client-id",
            clientSecret = "your-client-secret" // Optional
        )
        
        // Observe token state
        lifecycleScope.launch {
            tokenProvider.tokenState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun updateUI(state: TokenState) {
        when (state) {
            is TokenState.NoToken -> showLoginButton()
            is TokenState.Refreshing -> showLoading()
            is TokenState.Valid -> showAuthenticatedUI()
            is TokenState.Invalid -> showError(state.message)
        }
    }
}
```

### Get an Access Token

```kotlin
private fun getAccessToken() {
    lifecycleScope.launch {
        showLoading()
        
        when (val result = tokenProvider.getAccessToken()) {
            is TokenResult.Success -> {
                val tokenSet = result.data
                // Use tokenSet.accessToken for API calls
                callApi(tokenSet.accessToken)
            }
            is TokenResult.Error -> {
                showError("Failed to get access token: ${result.exception.message}")
            }
        }
    }
}

private fun callApi(accessToken: String) {
    // Example of using the access token
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.example.com/data")
        .header("Authorization", "Bearer $accessToken")
        .build()
    
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            // Handle response
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    showData(response.body?.string())
                } else {
                    showError("API error: ${response.code}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showError("Network error: ${e.message}")
            }
        }
    }
}
```

### Handle Auto Login Code

```kotlin
private fun requestAutoLoginCode() {
    lifecycleScope.launch {
        showLoading()
        
        when (val result = tokenProvider.requestAutoLoginCode(
            username = "user@example.com",
            additionalParams = mapOf("redirect_uri" to "myapp://callback")
        )) {
            is TokenResult.Success -> {
                val code = result.data
                showAutoLoginCode(code)
            }
            is TokenResult.Error -> {
                showError("Failed to get auto login code: ${result.exception.message}")
            }
        }
    }
}

private fun showAutoLoginCode(code: String) {
    // Display the code to the user
    findViewById<TextView>(R.id.autoLoginCodeText).text = code
    findViewById<View>(R.id.autoLoginCodeContainer).visibility = View.VISIBLE
}
```

## Advanced Integration

### Application-wide TokenProvider

For application-wide access to the TokenProvider, initialize it in your Application class:

```kotlin
class MyApplication : Application() {
    // Lazy initialization to ensure proper context availability
    val tokenProvider: TokenProvider by lazy {
        TokenProvider.create(
            engine = Android.create(),
            storage = AndroidSecureStorage(applicationContext),
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
            issuerUrl = "https://example.auth0.com",
            clientId = "your-client-id"
        )
    }
}

// Access in activities/fragments
val tokenProvider = (application as MyApplication).tokenProvider
```

### API Client Integration

Create a network interceptor to automatically add tokens to API requests:

```kotlin
class TokenAuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val appCoroutineScope: CoroutineScope
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip authentication for token endpoint or other public endpoints
        if (originalRequest.url.toString().contains("token")) {
            return chain.proceed(originalRequest)
        }
        
        // Get token synchronously (for OkHttp interceptor)
        val tokenResult = runBlocking {
            tokenProvider.getAccessToken()
        }
        
        return when (tokenResult) {
            is TokenResult.Success -> {
                val token = tokenResult.data.accessToken
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(authenticatedRequest)
            }
            is TokenResult.Error -> {
                // Log the error and notify the app
                appCoroutineScope.launch {
                    // Emit an event to notify the app about the auth error
                    // This could be done via EventBus, Flow, LiveData, etc.
                }
                
                // Proceed with the original request (will likely fail)
                chain.proceed(originalRequest)
            }
        }
    }
}

// Create OkHttpClient with the interceptor
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(TokenAuthInterceptor(tokenProvider, lifecycleScope))
    .build()
```

### Using with Retrofit

```kotlin
// API service interface
interface ApiService {
    @GET("user/profile")
    suspend fun getUserProfile(): UserProfile
}

// Retrofit setup
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient) // OkHttpClient with TokenAuthInterceptor
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)

// Using the API service
lifecycleScope.launch {
    try {
        val profile = apiService.getUserProfile()
        displayProfile(profile)
    } catch (e: Exception) {
        handleApiError(e)
    }
}
```

### Handling Token Expiration

The SDK handles token expiration automatically, but you can also implement additional logic:

```kotlin
// Check if token will expire soon
fun isTokenExpiringSoon(tokenSet: TokenSet, thresholdSeconds: Int = 60): Boolean {
    val currentTime = System.currentTimeMillis() / 1000
    return tokenSet.expiresAt - currentTime < thresholdSeconds
}

// Proactively refresh token
fun refreshTokenIfNeeded() {
    lifecycleScope.launch {
        val tokenResult = tokenProvider.getTokens()
        if (tokenResult != null && isTokenExpiringSoon(tokenResult)) {
            tokenProvider.getAccessToken(forceRefresh = true)
        }
    }
}
```

### Error Handling

Implement comprehensive error handling:

```kotlin
private fun handleTokenError(error: Throwable) {
    val errorMessage = when (error) {
        is IOException -> {
            // Network error
            "Network error. Please check your connection and try again."
        }
        is SecurityException -> {
            // Security error (e.g., keystore access)
            "Security error. Please restart the app and try again."
        }
        is IllegalStateException -> {
            // State error (e.g., configuration issue)
            "Configuration error. Please contact support."
        }
        else -> {
            // Unknown error
            "Unknown error: ${error.message}. Please try again later."
        }
    }
    
    // Log detailed error for debugging
    Log.e("TokenProvider", "Token error", error)
    
    // Show user-friendly message
    showError(errorMessage)
    
    // Take appropriate action
    if (error.message?.contains("invalid_grant") == true) {
        // Token is invalid, show login screen
        showLoginScreen()
    }
}
```

## Testing

### Mock TokenProvider for Testing

```kotlin
// Mock implementation of TokenProvider for testing
class MockTokenProvider : TokenProvider {
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.NoToken)
    override val tokenState: StateFlow<TokenState> = _tokenState.asStateFlow()
    
    // Mock tokens
    private val mockTokenSet = TokenSet(
        accessToken = "mock_access_token",
        refreshToken = "mock_refresh_token",
        tokenType = "Bearer",
        scope = "openid profile email",
        expiresAt = System.currentTimeMillis() / 1000 + 3600,
        autoLoginCode = null
    )
    
    // Mock success or failure flag
    var shouldSucceed = true
    
    override suspend fun getAccessToken(forceRefresh: Boolean): TokenResult<TokenSet> {
        return if (shouldSucceed) {
            _tokenState.value = TokenState.Valid(mockTokenSet)
            TokenResult.Success(mockTokenSet)
        } else {
            _tokenState.value = TokenState.Invalid("Mock error")
            TokenResult.Error(RuntimeException("Mock error"))
        }
    }
    
    override suspend fun getAutoLoginCode(): String? {
        return "mock_auto_login_code"
    }
    
    override suspend fun requestAutoLoginCode(
        username: String,
        additionalParams: Map<String, String>
    ): TokenResult<String> {
        return if (shouldSucceed) {
            TokenResult.Success("mock_auto_login_code")
        } else {
            TokenResult.Error(RuntimeException("Mock error"))
        }
    }
}

// Using the mock in tests
@Test
fun testGetAccessToken_success() = runTest {
    // Arrange
    val mockTokenProvider = MockTokenProvider()
    mockTokenProvider.shouldSucceed = true
    
    // Act
    val result = mockTokenProvider.getAccessToken()
    
    // Assert
    assertTrue(result is TokenResult.Success)
    assertEquals("mock_access_token", (result as TokenResult.Success).data.accessToken)
}
```

## Best Practices for Android

1. **Use lifecycle-aware coroutine scopes**: Always use lifecycle-aware coroutine scopes to prevent memory leaks.
2. **Handle configuration changes**: Ensure your token state handling survives configuration changes.
3. **Implement proper error UX**: Follow Material Design guidelines for error states.
4. **Use dependency injection**: Consider using Hilt or Koin for dependency injection.
5. **Secure storage**: Be aware of security implications on rooted devices.
6. **Implement proper logging**: Use obfuscated logging for sensitive operations.
7. **Handle process death**: Save and restore essential state across process death.

## Troubleshooting

### Common Issues

1. **Keystore errors**: On some devices, the Keystore may not be available or may have issues. Implement fallback mechanisms.
2. **Network timeouts**: Token operations may timeout on slow networks. Implement proper timeout handling.
3. **Coroutine cancellation**: Be careful with coroutine cancellation during token operations.

### Debugging

Enable detailed logging for debugging:

```kotlin
// Create TokenProvider with logging
val tokenProvider = TokenProvider.create(
    engine = Android.create(),
    storage = AndroidSecureStorage(context),
    coroutineScope = lifecycleScope,
    issuerUrl = "https://example.auth0.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
)

// Add network logging interceptor
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()
```

## Conclusion

The KMM OIDC Token Provider SDK provides a robust solution for managing OIDC authentication in Android applications. By following the integration guidelines in this document, you can implement secure and user-friendly authentication flows in your app.

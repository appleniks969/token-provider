# KMM OIDC Token Provider SDK - Best Practices

This document outlines best practices for using the KMM OIDC Token Provider SDK in your applications.

## Security Best Practices

### Token Storage

- **Use the provided secure storage**: The SDK includes platform-specific secure storage implementations that use Android Keystore and iOS Keychain.
- **Never store tokens in plain text**: Always use the secure storage mechanisms provided by the SDK.
- **Clear tokens on logout**: Call `clearTokens()` when the user logs out to remove stored tokens.

```kotlin
// Android example for clearing tokens on logout
lifecycleScope.launch {
    storage.clearTokens()
    // Update UI to show logged out state
}
```

### PKCE Implementation

- **Enable PKCE for mobile apps**: When the SDK adds PKCE support, always enable it for mobile applications.
- **Use S256 code challenge method**: The S256 method provides stronger security than plain.
- **Store verifier securely**: The code verifier should be stored securely until the authorization code is exchanged.

### Token Validation

- **Validate tokens**: Check token expiration and validity before using them.
- **Implement proper error handling**: Handle token validation errors gracefully.
- **Use the token state flow**: Monitor the token state to react to token validity changes.

```kotlin
// Android example for observing token state
lifecycleScope.launch {
    tokenProvider.tokenState.collect { state ->
        when (state) {
            is TokenState.NoToken -> showLoginUI()
            is TokenState.Refreshing -> showLoadingUI()
            is TokenState.Valid -> showAuthenticatedUI()
            is TokenState.Invalid -> handleInvalidToken(state.message)
        }
    }
}
```

### General Security

- **Use HTTPS**: Always use HTTPS for all OIDC endpoints.
- **Implement proper error messages**: Don't expose sensitive information in error messages.
- **Validate redirect URIs**: Ensure redirect URIs are properly validated.

## Performance Best Practices

### Token Caching

- **Use the built-in caching**: The SDK handles token caching automatically.
- **Configure appropriate token lifetimes**: Work with your OIDC provider to set appropriate token lifetimes.
- **Be mindful of refresh token lifetimes**: Refresh tokens may have longer lifetimes but should still be rotated periodically.

### Network Optimization

- **Handle network errors gracefully**: Implement proper retries and fallbacks for network operations.
- **Consider offline scenarios**: Cache essential data for offline use.
- **Monitor network performance**: Track token refresh times and other network operations.

### Memory Management

- **Be mindful of coroutine scope**: Provide an appropriate coroutine scope that aligns with your component lifecycle.
- **Cancel ongoing operations**: Cancel coroutines when components are destroyed.

```kotlin
// Android example for providing lifecycle-aware coroutine scope
val tokenProvider = TokenProvider.create(
    engine = Android.create(),
    storage = AndroidSecureStorage(context),
    coroutineScope = viewLifecycleOwner.lifecycleScope, // Use lifecycle-aware scope
    issuerUrl = "https://example.auth0.com",
    clientId = "your-client-id"
)
```

## User Experience Best Practices

### Authentication Flow

- **Handle auth state gracefully**: Provide clear UI feedback during authentication.
- **Implement proper loading states**: Show loading indicators during token operations.
- **Handle errors user-friendly**: Translate technical errors into user-friendly messages.

### Token Refresh

- **Refresh tokens proactively**: Refresh tokens before they expire to prevent disruption.
- **Handle failed refreshes**: Provide clear guidance when refresh fails.
- **Implement graceful degradation**: Allow limited functionality when authentication fails.

### Error Handling

- **Provide actionable error messages**: Help users understand what went wrong and how to fix it.
- **Log detailed errors**: Log detailed errors for debugging while showing simplified messages to users.
- **Implement retry mechanisms**: Allow users to retry failed operations.

```kotlin
// Android example for handling token errors
when (val result = tokenProvider.getAccessToken()) {
    is TokenResult.Success -> {
        // Use token for API calls
    }
    is TokenResult.Error -> {
        val userMessage = when (result.exception) {
            is IOException -> "Network error. Please check your connection."
            is IllegalStateException -> "Authentication error. Please log in again."
            else -> "Unknown error. Please try again later."
        }
        showErrorToUser(userMessage)
        logDetailedError(result.exception)
    }
}
```

## Integration Best Practices

### SDK Initialization

- **Initialize early**: Initialize the SDK early in your application lifecycle.
- **Handle initialization errors**: Gracefully handle errors during SDK initialization.
- **Configure appropriately**: Use appropriate configuration for your use case.

```kotlin
// Android example for early initialization
class MyApplication : Application() {
    lateinit var tokenProvider: TokenProvider
    
    override fun onCreate() {
        super.onCreate()
        
        tokenProvider = TokenProvider.create(
            engine = Android.create(),
            storage = AndroidSecureStorage(applicationContext),
            coroutineScope = GlobalScope, // Be careful with GlobalScope
            issuerUrl = "https://example.auth0.com",
            clientId = "your-client-id"
        )
    }
}
```

### API Integration

- **Use dependency injection**: Provide the TokenProvider through dependency injection.
- **Separate auth logic**: Keep authentication logic separate from business logic.
- **Create auth interceptors**: Implement network interceptors to add tokens to API calls.

```kotlin
// Android example for OkHttp interceptor
class TokenAuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token synchronously (not ideal, but required for OkHttp interceptor)
        val token = runBlocking {
            val result = tokenProvider.getAccessToken()
            if (result is TokenResult.Success) {
                result.data.accessToken
            } else {
                null
            }
        }
        
        return if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
```

### Testing

- **Mock the TokenProvider**: Create mock implementations for testing.
- **Test error scenarios**: Ensure your application handles authentication errors gracefully.
- **Test token expiration**: Verify that token expiration is handled correctly.

## OIDC Provider Configuration

- **Configure appropriate scopes**: Only request the scopes your application needs.
- **Set appropriate token lifetimes**: Configure token lifetimes based on your security requirements.
- **Use appropriate client type**: Configure your client properly (public vs. confidential).
- **Enable refresh token rotation**: Use refresh token rotation for enhanced security when available.

## Platform-Specific Considerations

### Android

- **Handle lifecycle properly**: Use lifecycle-aware coroutine scopes.
- **Consider process death**: Handle process death and restoration gracefully.
- **Implement proper error UX**: Follow Android design guidelines for error handling.

### iOS

- **Use Swift extensions**: Create Swift extensions for more idiomatic API usage.
- **Handle memory management**: Be mindful of reference cycles with closures.
- **Follow iOS design guidelines**: Implement authentication UX according to iOS guidelines.

```swift
// iOS example for Swift extensions
extension TokenProvider {
    func getAccessToken(completion: @escaping (Result<TokenSet, Error>) -> Void) {
        TokenProviderKt.getAccessToken(self) { result in
            if let success = result as? TokenResult.Success<TokenSet> {
                completion(.success(success.data))
            } else if let error = result as? TokenResult.Error {
                completion(.failure(error.exception))
            }
        }
    }
}
```

## Conclusion

Following these best practices will help you integrate the KMM OIDC Token Provider SDK effectively and securely in your applications. Always prioritize security and user experience when implementing authentication flows.

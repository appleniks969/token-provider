# KMM OIDC Token Provider SDK Documentation

## Overview

The OIDC Token Provider SDK is a Kotlin Multiplatform library that simplifies the process of obtaining and refreshing OpenID Connect (OIDC) tokens on both Android and iOS platforms. It handles the complexities of OIDC token management, including discovery, token acquisition, secure storage, and automatic refreshing.

## Core Concepts

### OpenID Connect (OIDC)

OpenID Connect is an identity authentication protocol built on top of OAuth 2.0. It allows clients to verify the identity of end-users and obtain basic profile information in a secure and standardized way.

Key components:
- **Discovery Document** - Available at `.well-known/openid-configuration`, contains endpoints and capabilities
- **Access Token** - Used to access protected resources
- **Refresh Token** - Used to obtain new access tokens
- **Auto Login Code** - Used for simplified login on different devices

## API Reference

### TokenProvider

The main entry point for the SDK.

#### Creation

```kotlin
// Standard creation
val tokenProvider = TokenProvider.create(
    engine = httpEngine,
    storage = secureStorage,
    coroutineScope = coroutineScope,
    issuerUrl = "https://example.auth0.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret" // Optional
)
```

#### Methods

##### `getAccessToken`

Gets a valid access token, handling discovery and refresh automatically.

```kotlin
suspend fun getAccessToken(forceRefresh: Boolean = false): TokenResult<TokenSet>
```

**Parameters:**
- `forceRefresh`: Whether to force refresh even if the token isn't expired

**Returns:**
- `TokenResult<TokenSet>`: Success with token set or Error

**Example:**
```kotlin
when (val result = tokenProvider.getAccessToken()) {
    is TokenResult.Success -> {
        val tokenSet = result.data
        // Use tokenSet.accessToken
    }
    is TokenResult.Error -> {
        // Handle error
    }
}
```

##### `getAutoLoginCode`

Gets a stored auto login code.

```kotlin
suspend fun getAutoLoginCode(): String?
```

**Returns:**
- Auto login code or null if not available

**Example:**
```kotlin
val code = tokenProvider.getAutoLoginCode()
if (code != null) {
    // Use the code
}
```

##### `requestAutoLoginCode`

Requests a new auto login code from the OIDC provider.

```kotlin
suspend fun requestAutoLoginCode(
    username: String,
    additionalParams: Map<String, String> = emptyMap()
): TokenResult<String>
```

**Parameters:**
- `username`: Username for which to generate the code
- `additionalParams`: Additional parameters required by the provider

**Returns:**
- `TokenResult<String>`: Success with auto login code or Error

**Example:**
```kotlin
when (val result = tokenProvider.requestAutoLoginCode(
    username = "user@example.com",
    additionalParams = mapOf("redirect_uri" to "myapp://callback")
)) {
    is TokenResult.Success -> {
        val code = result.data
        // Use the code
    }
    is TokenResult.Error -> {
        // Handle error
    }
}
```

#### Properties

##### `tokenState`

A flow of token state updates.

```kotlin
val tokenState: StateFlow<TokenState>
```

**Example:**
```kotlin
tokenProvider.tokenState.collect { state ->
    when (state) {
        is TokenState.NoToken -> // No token available
        is TokenState.Refreshing -> // Token is being refreshed
        is TokenState.Valid -> // Token is valid: state.tokens contains the tokens
        is TokenState.Invalid -> // Token is invalid: state.message contains the error
    }
}
```

### SecureStorage

Interface for token storage.

```kotlin
interface SecureStorage {
    suspend fun saveTokens(tokens: TokenSet)
    suspend fun getTokens(): TokenSet?
    suspend fun clearTokens()
}
```

Platform-specific implementations:
- `AndroidSecureStorage` - Uses Android Keystore and SharedPreferences
- `IOSSecureStorage` - Uses iOS Keychain Services

### Data Models

#### TokenSet

Represents a set of tokens.

```kotlin
data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val scope: String?,
    val expiresAt: Long,
    val autoLoginCode: String?
) {
    fun isExpired(timestampInSeconds: Long = currentTimeInSeconds()): Boolean
}
```

#### TokenResult

Sealed class for handling operation results.

```kotlin
sealed class TokenResult<out T> {
    data class Success<T>(val data: T) : TokenResult<T>()
    data class Error(val exception: Throwable) : TokenResult<Nothing>()
}
```

#### TokenState

Sealed class representing the current state of tokens.

```kotlin
sealed class TokenState {
    object NoToken : TokenState()
    object Refreshing : TokenState()
    data class Valid(val tokens: TokenSet) : TokenState()
    data class Invalid(val message: String) : TokenState()
}
```

## Platform-Specific Usage

### Android

```kotlin
// In your Activity or ViewModel
val storage = AndroidSecureStorage(applicationContext)

val tokenProvider = TokenProvider.create(
    engine = Android.create(),
    storage = storage,
    coroutineScope = lifecycleScope,
    issuerUrl = "https://example.auth0.com",
    clientId = "your-client-id",
    clientSecret = "your-client-secret" // Optional
)

lifecycleScope.launch {
    val result = tokenProvider.getAccessToken()
    // Handle result
}
```

### iOS

```swift
// In your ViewController or ViewModel
let storage = IOSSecureStorage()
let engine = DarwinHttpClientEngine()
let scope = MainScope()

let tokenProvider = TokenProviderKt.create(
    engine: engine,
    storage: storage,
    coroutineScope: scope,
    issuerUrl: "https://example.auth0.com",
    clientId: "your-client-id",
    clientSecret: "your-client-secret" // Optional
)

TokenProviderKt.getAccessToken(tokenProvider) { result in
    // Handle result
}
```

## Security Best Practices

1. **Token Storage**
   - Tokens are stored securely using platform-specific mechanisms
   - Android uses Keystore and encrypted SharedPreferences
   - iOS uses Keychain Services

2. **Token Refresh**
   - Tokens are automatically refreshed before they expire
   - Buffer time is added to prevent using tokens near expiration
   - Clock skew is handled to account for client-server time differences

3. **Communication Security**
   - All network communication uses HTTPS
   - Error responses are properly handled and do not expose sensitive information

## Troubleshooting

### Common Issues

1. **Discovery fails**
   - Ensure the issuerUrl is correct
   - Check network connectivity
   - Verify that the OIDC provider is operational

2. **Token refresh fails**
   - Check that the refresh token is valid
   - Ensure the client ID and secret are correct
   - Verify that the refresh token hasn't been revoked

3. **Token storage issues**
   - Ensure the app has proper permissions
   - On Android, check if the device has a supported Keystore implementation
   - On iOS, verify that Keychain access is working properly
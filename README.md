# KMM OIDC Token Provider SDK

A Kotlin Multiplatform Mobile (KMM) SDK for managing OpenID Connect (OIDC) tokens on both Android and iOS platforms. This SDK provides a unified API for discovering OIDC configurations, obtaining and refreshing tokens, and securely storing them.

## Features

- **Discovery** - Automatically discover OIDC provider endpoints
- **Token Management** - Get, refresh, and store access tokens
- **Auto Login Codes** - Support for requesting and storing auto login codes
- **Secure Storage** - Platform-specific secure storage for tokens
- **Multiple Token Support** - Store and manage tokens for different scopes and resources
- **Reactive State** - Kotlin Flow-based state updates

## Architecture

The SDK follows a clean architecture design with the following components:

- **TokenProvider** - Main entry point for the SDK that manages token lifecycle
- **TokenClient** - Handles communication with OIDC endpoints
- **TokenRepository** - Manages secure storage of tokens
- **Domain Models** - Token-related data models

## Installation

*Add gradle/cocoapods dependency information here*

## Usage

### Initialize the SDK

```kotlin
// Android
val tokenProvider = AndroidExample.createTokenProvider(context)

// iOS
val tokenProvider = IOSExample.createTokenProvider()
```

### Discover the OIDC Configuration

```kotlin
coroutineScope.launch {
    val result = tokenProvider.discoverConfiguration("https://example.auth0.com")
    
    when (result) {
        is TokenResult.Success -> {
            println("Discovered OIDC configuration")
            println("Token endpoint: ${result.data.tokenEndpoint}")
        }
        is TokenResult.Error -> {
            println("Failed to discover: ${result.exception.message}")
        }
    }
}
```

### Get an Access Token

```kotlin
coroutineScope.launch {
    val result = tokenProvider.getAccessToken(
        clientId = "your-client-id",
        clientSecret = "your-client-secret" // Optional
    )
    
    when (result) {
        is TokenResult.Success -> {
            val accessToken = result.data
            // Use the token for API calls
        }
        is TokenResult.Error -> {
            println("Failed to get token: ${result.exception.message}")
        }
    }
}
```

### Request an Auto Login Code

```kotlin
coroutineScope.launch {
    val result = tokenProvider.requestAutoLoginCode(
        clientId = "your-client-id",
        username = "user@example.com",
        clientSecret = "your-client-secret", // Optional
        additionalParams = mapOf(
            "redirect_uri" to "myapp://callback"
        )
    )
    
    when (result) {
        is TokenResult.Success -> {
            val code = result.data
            println("Got auto login code: $code")
        }
        is TokenResult.Error -> {
            println("Failed to get code: ${result.exception.message}")
        }
    }
}
```

### Multiple Token Scopes (with ScopedTokenRepository)

```kotlin
// Create a scope for read operations
val readScope = TokenScope.forScope(
    clientId = "your-client-id",
    scope = "read",
    userId = "user123"
)

// Get tokens for the read scope
val readTokens = scopedRepository.getTokens(readScope)
```

## Platform-Specific Considerations

### Android

The Android implementation uses the Android Keystore System and SharedPreferences for secure token storage.

### iOS

The iOS implementation uses the Keychain Services API for secure token storage.

## License

*Add license information here*